package org.awspdi;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.amazonaws.services.s3.model.StaticEncryptionMaterialsProvider;
import org.apache.commons.codec.binary.Base64;
import org.awspdi.encrypt.GenerateSymmetricMasterKey;
import org.awspdi.upload.EncryptedUploader;
import org.awspdi.upload.IUploader;
import org.awspdi.upload.Uploader;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *  S3 Uploader is what handles management
 *  of leveraging the appropriate S3 service
 *  and uploading the file(s) from a given path.
 *  
 * @author Kristofer Ranström
 *
 */
public class AwsUploadToS3 {

	private static boolean ENV_CREDS = false;
	private static AwsProperties awsProperties;
	private static AWSCredentialsProvider awsCredentials;
	private static String filePath;

	/**
	 * 
	 * @param props awsProperties
	 * @param pathToLoad path to be loaded
	 */
	public AwsUploadToS3(final AwsProperties props, 
			final String pathToLoad) {
		awsProperties = props;
		filePath = pathToLoad;

		awsCredentials = null;

		if (!(null == System.getenv("AWS_ACCESS_KEY_ID") &&
				null == System.getenv("AWS_SECRET_ACCESS_KEY"))) {

			System.out.println("Loading credentials from env...");
			ENV_CREDS = true;

		} else {

			System.out.println("Loading credentials from file...");
			ENV_CREDS = false;

			try {
				ProfilesConfigFile profileConfig = new ProfilesConfigFile(
						awsProperties.awsProfilePath);

				awsCredentials = new ProfileCredentialsProvider(
						profileConfig, awsProperties.awsProfileName);

			} catch (Exception e) {
				throw new AmazonClientException(
						"Cannot load the credentials from the credential "
								+ "profiles file.", e);
			}

		}

	}
	
    
    /**
     *  This is the uploader that is called after
     *  class is instantiated.
     */
    public final void uploadToS3Manager() throws Exception {

		AmazonS3Client awsClient;
		if (awsProperties.awsSendEncrypted) {

			GenerateSymmetricMasterKey newKey;

			// If key is not populated we need to generate one
			String msk = awsProperties.awsMasterSymmetricKey;
			if (!awsProperties.awsMSKPopulated) {
				newKey = new GenerateSymmetricMasterKey(
						awsProperties.awsLocalKeyDir, "Key.key",
						awsProperties.awsAlgorithm,
						awsProperties.awsAlgorithmKeyLength);
				msk = newKey.getSymmetricMasterKey();
			}

			SecretKey symmetricKey = new SecretKeySpec(
			            Base64.decodeBase64(msk.getBytes()),
			           awsProperties.awsAlgorithm);

			EncryptionMaterials materials = new EncryptionMaterials(symmetricKey);

			StaticEncryptionMaterialsProvider matsProvider = new StaticEncryptionMaterialsProvider(materials);
			awsClient = new AmazonS3EncryptionClient(awsCredentials, matsProvider);

			awsClient.setEndpoint(awsProperties.s3endpoint);
			// awsEncryptionClient.setRegion(awsProperties.s3region);
		} else {
			System.out.println("Sending file unencrypted...");
			awsClient = new AmazonS3Client(awsCredentials);
			awsClient.setEndpoint(awsProperties.s3endpoint);
			// awsClient.setRegion(awsProperties.s3region);
		}


    	File file = new File(filePath);
    	
    	if (file.exists() && file.isDirectory()) {
			System.out.println("Initiating folder upload...");
    		retrieveAllFilesAndUpload(awsClient, file);
    	} else if (file.exists() && file.isFile()) {
			System.out.println("Initiating single file upload...");
    		String adjustedFilePath = filePath;
    		
			// Gzip file for smaller footprint. Redshift can handle this later
			if (awsProperties.awsEnableZip) {
		    	CompressFileGzip gZipFile = new CompressFileGzip();
				gZipFile.gzipFile(filePath,
						awsProperties.awsLocalDataDir);
				adjustedFilePath = awsProperties.awsLocalDataDir
						+ "/" + file.getName() + ".gzip";
			}

			IUploader uploader;

            if (awsProperties.awsSendEncrypted) {
				uploader = new EncryptedUploader(awsClient);
    		} else {
				uploader = new Uploader(awsClient);
    		}

			uploader.setUploadContent(adjustedFilePath);
			uploader.setUploadContext(awsProperties);
			uploader.upload();
    	}
    }
	
    /**
     * In a bulk load scenario we need to call the service
     * multiple times. This manages calling the appropriate
     * services for each load.
     * 
     * @param folder to retrieve files from
     */
    private static void retrieveAllFilesAndUpload(AmazonS3Client client, final File folder) {

		HashMap<String, String> keyMap = new HashMap<String, String>();

        File fileFolder;        
        if (awsProperties.awsEnableZip) {
        	fileFolder = new File(awsProperties.awsLocalDataDir);

        	// Zip file if this has been enabled
        	for (final File fileToZip : folder.listFiles()) {
    			if (awsProperties.awsEnableZip) {
    		    	CompressFileGzip gZipFile = new CompressFileGzip();
    				gZipFile.gzipFile(fileToZip.getAbsolutePath(),
    						awsProperties.awsLocalDataDir);				
    			}
        	}
        } else {
        	fileFolder = folder;
        }

        System.out.println("Reading files from directory " + fileFolder);

        for (final File fileEntry : fileFolder.listFiles()) {

        	// If zipped then we only want to pull gzip files from dir
        	// Else we pull all files.
        	// This is to preserve scenario where unzipped files are located
        	// in the same directory as the zipped files
        	boolean zippedOrNot =  ((awsProperties.awsEnableZip 
        			&& fileEntry.getName().toLowerCase().endsWith(".gzip")) 
        			|| !awsProperties.awsEnableZip);
        	
        	// Skip sub directories and if encrypted only pull gzip files
            if (!fileEntry.isDirectory() && zippedOrNot) {
            	
                int retryCounter = 0;
                boolean done = false;
                while (!done) {
                    try {

                        if (awsProperties.awsSendEncrypted) {

							EncryptedUploader uploader = new EncryptedUploader(client);
							uploader.setUploadContent(fileEntry);
							uploader.setUploadContext(awsProperties);
							uploader.upload();

                			// If MSK is not populated we add keys to
                			// a keymap that will be saved/stdouted
                			if (!awsProperties.awsMSKPopulated) {
                				keyMap.put(fileEntry.getName(), 
                						uploader.getNewKey()
                						.getSymmetricMasterKey());
                			}

                        	System.out.println("Uploaded: "
                        			+ fileEntry.toString());
                		} else {
							Uploader uploader = new Uploader(client);
							uploader.setUploadContent(fileEntry);
							uploader.setUploadContext(awsProperties);
							uploader.upload();

                        	System.out.println("Uploaded: "
                        			+ fileEntry.toString());
                		}

                        done = true;

                    } catch (Exception e) {
						// TODO: I don't think this was ever actually doing anything...
                        // retryCounter++;
                        // if (retryCounter > awsProperties.awsRetryCount) {
                        //     System.out.println(
                        //     		"Retry count exceeded max retry count "
                        //                     + awsProperties.awsRetryCount
                        //                     + ". Giving up");
                        //     throw new RuntimeException(e);
                        // }

                        // // Do retry after 10 seconds.
                        // System.out.println("Failed to upload file " + fileEntry
                        //         + ". Retrying...");
                        // try {
                        //     Thread.sleep(10 * 1000);
                        // } catch (Exception te) {
					}
				}
			}
		}
		// Only saves if applicable via awsProperties
		EncryptedUploader.saveOrOutputKeyMap(keyMap, awsProperties);
	}
}

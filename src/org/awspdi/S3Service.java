package org.awspdi;

import java.io.File;
//import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.awspdi.encrypt.GenerateSymmetricMasterKey;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
//import com.amazonaws.regions.Region;
//import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.amazonaws.services.s3.model.PutObjectRequest;

/**
 * Many thanks to: http://yoshidashingo.hatenablog.com/entry/2014/08/10/193631
 * for some valuable suggestions/additions.
 * 
 * @author Kristofer Ranström
 *
 */
public final class S3Service {
	
	private static String s3bucket;
	private static String s3prefix;
    private static String s3endpoint;
    private static String masterSymmetricKey;
    private static String algorithm;
    private static int algorithmKeyLength;
    private static int retryCount;
    private static String keyDir;
	private static boolean enableZip;
	private static boolean sendEncrypted;
	private static AWSCredentials awsCredentials;
	private static AmazonS3EncryptionClient awsEncryptionClient;
	private static AmazonS3 awsClient;
	private static EncryptionMaterials materials;
    
    /**
     * 
     * @param awsProperties props
     */
    S3Service(final AwsProperties awsProperties) {
    	
    	s3bucket = awsProperties.getS3bucket();
    	s3prefix = awsProperties.getS3prefix();
    	s3endpoint = awsProperties.getS3endpoint();
    	masterSymmetricKey = awsProperties.getMasterSymmetricKey();
    	algorithm = awsProperties.getAlgorithm();
    	algorithmKeyLength = awsProperties.getAlgorithmKeyLength();
    	retryCount = awsProperties.getRetryCount();
    	keyDir = awsProperties.getKeyDir();
    	enableZip = awsProperties.isEnableZip();
    	sendEncrypted = awsProperties.isSendEncrypted();

        try {
        	ProfilesConfigFile profileConfig = new ProfilesConfigFile(
        			awsProperties.getAwsProfilePath());
            awsCredentials = new ProfileCredentialsProvider(
            		profileConfig, "AWSProfile").getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential "
            		+ "profiles file.", e);
        }
        
        if (sendEncrypted) {
        	String msk = masterSymmetricKey;
        	if (msk == null || msk.trim().isEmpty()) {
        		GenerateSymmetricMasterKey newKey = 
        				new GenerateSymmetricMasterKey(keyDir, "NewKey.key", 
        						algorithm, algorithmKeyLength);
        		msk = newKey.getSymmetricMasterKey();
        	}
        	
        	SecretKey symmetricKey = new SecretKeySpec(
                        Base64.decodeBase64(msk.getBytes()), algorithm);

            materials = new EncryptionMaterials(symmetricKey);        

            awsEncryptionClient = new AmazonS3EncryptionClient(awsCredentials, 
            		materials);
            awsEncryptionClient.setEndpoint(s3endpoint);        	
        } else {
        	awsClient = new AmazonS3Client(awsCredentials);
        	awsClient.setEndpoint(s3endpoint);
//          Region usWest2 = Region.getRegion(Regions.US_WEST_2);
//        	awsClient.setRegion(usWest2);
        }
    }
    
    /**
     * 
     * @param filePath to upload (excluding .gzip)
     */
    public void uploadToS3(final String filePath) {
    	
    	File file = new File(filePath);
    	
    	if (file.exists() && file.isDirectory()) {
    		retrieveAllFilesAndUpload(file);
    	} else if (file.exists() && file.isFile()) {
    		String adjustedFilePath = filePath + (enableZip ? ".gzip" : "");
        	File fileToUpload = new File(adjustedFilePath);

            if (sendEncrypted) {
    			uploadToS3Encrypted(fileToUpload);
    		} else {
    			uploadToS3Unencrypted(fileToUpload);
    		}    		
    	}    	
    }
    
    /**
     * 
     * @param fileToUpload 
     */
    public static void uploadToS3Unencrypted(final File fileToUpload) {

    	// TODO: add support for UUID random names
        // String objectKey = UUID.randomUUID().toString();

        try {
            System.out.println("Uploading a new object to S3 object '"
                    + s3prefix + "' from file " + fileToUpload.getName());

            System.out.println("Uploading a new object to S3 from a file\n");
            awsClient.putObject(new PutObjectRequest(s3bucket, 
            		fileToUpload.getName(), fileToUpload));

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means "
            		+ "your request made it to Amazon S3, but was rejected "
            		+ "with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means "
            		+ "the client encountered a serious internal problem while "
            		+ "trying to communicate with S3, such as not being able "
            		+ "to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }
    
    /**
     * 
     * @param fileToUpload 
     */
    public static void uploadToS3Encrypted(final File fileToUpload) {        

        try {
            System.out.println("Uploading a new object to S3 object '"
                    + s3prefix + "' from file " + fileToUpload.getName());
            String key = s3prefix + "/" + fileToUpload.getName();
            awsEncryptionClient.putObject(new PutObjectRequest(s3bucket, key, 
            		fileToUpload));

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means "
            		+ "your request made it to Amazon S3, but was rejected "
            		+ "with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());

            throw ase;
        } catch (AmazonClientException ace) {
            System.out
                    .println("Caught an AmazonClientException, which means the "
                    		+ "client encountered a serious internal problem "
                    		+ "while trying to communicate with S3, such as "
                    		+ "not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
            throw ace;
        }
    	
    }
    
    /**
     * 
     * @param folder to retrieve files from
     */
    private static void retrieveAllFilesAndUpload(final File folder) {

        System.out.println("Reading files from directory " + folder);

        for (final File fileEntry : folder.listFiles()) {
        	
        	// If encrypted then we only want to pull gzip files from dir
        	// Else we pull all files.
        	boolean encryptedOrNot =  (((sendEncrypted 
        			&& fileEntry.getName().toLowerCase().endsWith(".gzip")) 
        			|| !sendEncrypted) ? true : false);
        	
        	// Skip sub directories and if encrypted only pull gzip files
            if (!fileEntry.isDirectory() && encryptedOrNot) { 
            	
                int retryCounter = 0;
                boolean done = false;
                while (!done) {
                    try {
                        if (sendEncrypted) {
                			uploadToS3Encrypted(fileEntry);
                		} else {
                			uploadToS3Unencrypted(fileEntry);
                		}
                        done = true;
                    } catch (Exception e) {
                        retryCounter++;
                        if (retryCounter > retryCount) {
                            System.out.println(
                            		"Retry count exceeded max retry count "
                                            + retryCount + ". Giving up");
                            throw new RuntimeException(e);
                        }

                        // Do retry after 10 seconds.
                        System.out.println("Failed to upload file " + fileEntry
                                + ". Retrying...");
                        try {
                            Thread.sleep(10 * 1000);
                        } catch (Exception te) {
                        }

                    }
                } // while
            } // for
        }
    }
}
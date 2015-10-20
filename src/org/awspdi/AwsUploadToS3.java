package org.awspdi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.crypto.SecretKey;
import javax.xml.stream.events.StartDocument;

/**
 *  S3 Uploader is what handles management
 *  of leveraging the appropriate S3 service
 *  and uploading the file(s) from a given path.
 *  
 * @author Aenarion
 *
 */
public class AwsUploadToS3 {
	
	private static AwsProperties awsProperties;
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
	}
	
    
    /**
     *  This is the uploader that is called after
     *  class is instantiated.
     */
    public final void uploadToS3() {
    	
    	File file = new File(filePath);
    	
    	if (file.exists() && file.isDirectory()) {
    		retrieveAllFilesAndUpload(file);
    	} else if (file.exists() && file.isFile()) {
    		String adjustedFilePath = filePath;
    		
			// Gzip file for smaller footprint. Redshift can handle this later
			if (awsProperties.isEnableZip()) {
		    	CompressFileGzip gZipFile = new CompressFileGzip();
				gZipFile.gzipFile(filePath,
						awsProperties.getAwsLocalDataDir());
				adjustedFilePath = awsProperties.getAwsLocalDataDir()
						+ "/" + file.getName() + ".gzip";
			}
			
        	File fileToUpload = new File(adjustedFilePath);
        	
			S3Service s3Service = new S3Service(awsProperties);
            if (awsProperties.isSendEncrypted()) {
    			s3Service.uploadToS3Encrypted(fileToUpload);
    			printOrSaveKey(s3Service, file.getName());
    		} else {
    			s3Service.uploadToS3Unencrypted(fileToUpload);
    		}
    	}
    }
	
    /**
     * In a bulk load scenario we need to call the service
     * multiple times. This manages calling the appropriate
     * services for each load.
     * 
     * @param folder to retrieve files from
     */
    private static void retrieveAllFilesAndUpload(final File folder) {
        
        HashMap<String, String> keyMap = new HashMap<String, String>();

        File fileFolder;        
        if (awsProperties.isEnableZip()) {
        	fileFolder = new File(awsProperties.getAwsLocalDataDir());

        	// Zip file if this has been enabled
        	for (final File fileToZip : folder.listFiles()) {
    			if (awsProperties.isEnableZip()) {
    		    	CompressFileGzip gZipFile = new CompressFileGzip();
    				gZipFile.gzipFile(fileToZip.getAbsolutePath(),
    						awsProperties.getAwsLocalDataDir());				
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
        	boolean zippedOrNot =  (((awsProperties.isEnableZip() 
        			&& fileEntry.getName().toLowerCase().endsWith(".gzip")) 
        			|| !awsProperties.isEnableZip()) ? true : false);
        	
        	// Skip sub directories and if encrypted only pull gzip files
            if (!fileEntry.isDirectory() && zippedOrNot) {
            	
                int retryCounter = 0;
                boolean done = false;
                while (!done) {
                    try {
                        if (awsProperties.isSendEncrypted()) {
                			S3Service s3Service = new S3Service(awsProperties);
                			s3Service.uploadToS3Encrypted(fileEntry);
                			
                			// If MSK is not populated we add keys to
                			// a keymap that will be saved/stdouted
                			if (!awsProperties.isAwsMSKPopulated()) {
                				keyMap.put(fileEntry.getName(), 
                						s3Service.getNewKey()
                						.getSymmetricMasterKey());
                			}
                        	System.out.println("Uploaded: "
                        			+ fileEntry.toString());
                		} else {
                			S3Service s3Service = new S3Service(awsProperties);
                			s3Service.uploadToS3Unencrypted(fileEntry);
                        	System.out.println("Uploaded: "
                        			+ fileEntry.toString());
                		}
                        done = true;
                    } catch (Exception e) {
                        retryCounter++;
                        if (retryCounter > awsProperties.getAwsRetryCount()) {
                            System.out.println(
                            		"Retry count exceeded max retry count "
                                            + awsProperties.getAwsRetryCount() 
                                            + ". Giving up");
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
                }
            }
        }
        
        // If encrypted and MSK is not populated
        // then we need to save the newly generated keys.
        if (awsProperties.isSendEncrypted() 
        		&& !awsProperties.isAwsMSKPopulated()) {
        	saveOrOutputKeyMap(keyMap);
        }
    }
	
	/**
	 *  Method saves newly generated keys as a CSV
	 *  file into the local key directory.
	 *  If no directory is defined it will simply
	 *  output the contents to stdout.
	 * 
	 * @param keyMap key map to save
	 */
	private static void saveOrOutputKeyMap(
			final HashMap<String, String> keyMap) {
		
		if (!keyMap.isEmpty() && awsProperties.isOkToSaveKeys()) {
			try {
			    FileWriter writer = new FileWriter(awsProperties
			    		.getAwsLocalKeyDir() + "/keyMap.csv");
			    writer.append("Filename");
			    writer.append(',');
			    writer.append("MSK");
			    writer.append('\n');

			    for (Entry<String, String> entry : keyMap.entrySet()) {
				    writer.append(entry.getKey());
				    writer.append(',');
				    writer.append(entry.getValue());
			        writer.append('\n');
			    }
			    
			    writer.flush();
			    writer.close();
			} catch (IOException e) {
			     e.printStackTrace();
			}	
		} else if (!keyMap.isEmpty() && !awsProperties.isOkToSaveKeys()) {
			System.out.println("****BEGIN UPLOADED FILES & KEYS******");
			for (Entry<String, String> entry : keyMap.entrySet()) {
				System.out.println("File: " + entry.getKey()
						+ " | Key: " + entry.getValue());
		    }
			System.out.println("****END UPLOADED FILES & KEYS******");
		} else {
			System.out.println("Keymap is empty, nothing to write");
		}
	}
	
	/**
	 *  Save or print key depending on whether key directory is
	 *  specified.
	 *  
	 * @param s3Service that holds the key.
	 * @param fileName for saving keyfile
	 */
	private static void printOrSaveKey(final S3Service s3Service, 
			final String fileName) {
        // If MSK does not exist we need to save it
        if (!awsProperties.isAwsMSKPopulated() 
        		&& awsProperties.isOkToSaveKeys()) {
        	s3Service.getNewKey().saveSingleSymmetricKey(
        			 fileName + ".key", false);
        } else {
        	System.out.println("****BEGIN UPLOADED FILE & KEY******\n"
        			+ "File: " + fileName + " Key: " 
        			+ s3Service.getNewKey().getSymmetricMasterKey()
        			+ "\n****END UPLOADED FILE & KEY******");
        }
	}
}

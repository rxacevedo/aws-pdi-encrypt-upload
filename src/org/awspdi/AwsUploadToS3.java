package org.awspdi;

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
 * @author Aenarion
 *
 */
public class AwsUploadToS3 {
	
	private static AwsProperties awsProperties;
	private static String filePath;
	
	/**
	 * 
	 */
	private static final String LOG_UPLOAD_BEGIN = 
			"****BEGIN UPLOADED FILES & KEYS******";
	/**
	 * 
	 */
	private static final String LOG_UPLOAD_END = 
			"****END UPLOADED FILES & KEYS******";
	
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
    public final void uploadToS3Manager() {
    	
    	File file = new File(filePath);
    	
    	if (file.exists() && file.isDirectory()) {
    		retrieveAllFilesAndUpload(file);
    	} else if (file.exists() && file.isFile()) {
    		String adjustedFilePath = filePath;
    		
			// Gzip file for smaller footprint. Redshift can handle this later
			if (awsProperties.awsEnableZip) {
		    	CompressFileGzip gZipFile = new CompressFileGzip();
				gZipFile.gzipFile(filePath,
						awsProperties.awsLocalDataDir);
				adjustedFilePath = awsProperties.awsLocalDataDir
						+ "/" + file.getName() + ".gzip";
			}
			
        	File fileToUpload = new File(adjustedFilePath);
        	
			S3Service s3Service = new S3Service(awsProperties);
            if (awsProperties.awsSendEncrypted) {
    			s3Service.uploadToS3(fileToUpload);
    			printOrSaveKey(s3Service, file.getName());
    		} else {
    			s3Service.uploadMultiPartToS3Unencrypted(fileToUpload);
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
        	boolean zippedOrNot =  (((awsProperties.awsEnableZip 
        			&& fileEntry.getName().toLowerCase().endsWith(".gzip")) 
        			|| !awsProperties.awsEnableZip) ? true : false);
        	
        	// Skip sub directories and if encrypted only pull gzip files
            if (!fileEntry.isDirectory() && zippedOrNot) {
            	
                int retryCounter = 0;
                boolean done = false;
                while (!done) {
                    try {
                        if (awsProperties.awsSendEncrypted) {
                			S3Service s3Service = new S3Service(awsProperties);
                			s3Service.uploadToS3(fileEntry);
                			
                			// If MSK is not populated we add keys to
                			// a keymap that will be saved/stdouted
                			if (!awsProperties.awsMSKPopulated) {
                				keyMap.put(fileEntry.getName(), 
                						s3Service.getNewKey()
                						.getSymmetricMasterKey());
                			}
                        	System.out.println("Uploaded: "
                        			+ fileEntry.toString());
                		} else {
                			S3Service s3Service = new S3Service(awsProperties);
                			s3Service.uploadMultiPartToS3Unencrypted(fileEntry);
                        	System.out.println("Uploaded: "
                        			+ fileEntry.toString());
                		}
                        done = true;
                    } catch (Exception e) {
                        retryCounter++;
                        if (retryCounter > awsProperties.awsRetryCount) {
                            System.out.println(
                            		"Retry count exceeded max retry count "
                                            + awsProperties.awsRetryCount 
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
        if (awsProperties.awsSendEncrypted 
        		&& !awsProperties.awsMSKPopulated) {
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
		
		if (!keyMap.isEmpty()) {
			if (awsProperties.okToSaveKeys) {
				try {
				    FileWriter writer = new FileWriter(awsProperties
				    		.awsLocalKeyDir + "/keyMap.csv");
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
			} else {
				System.out.println(LOG_UPLOAD_BEGIN);
				for (Entry<String, String> entry : keyMap.entrySet()) {
					System.out.println("File: " + entry.getKey()
							+ " | Key: " + entry.getValue());
			    }
				System.out.println(LOG_UPLOAD_END);
			}
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
        if (!awsProperties.awsMSKPopulated 
        		&& awsProperties.okToSaveKeys) {
        	s3Service.getNewKey().saveSingleSymmetricKey(
        			 fileName + ".key", false);
        } else {
        	System.out.println(LOG_UPLOAD_BEGIN + "\n"
        			+ "File: " + fileName + " Key: " 
        			+ s3Service.getNewKey().getSymmetricMasterKey()
        			+ "\n" + LOG_UPLOAD_END);
        }
	}
}

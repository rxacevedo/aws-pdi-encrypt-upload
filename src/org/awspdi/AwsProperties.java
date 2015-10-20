package org.awspdi;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 
 * Loads the properties file from the specified path.
 * 
 * @author Kristofer Ranström
 *
 */
public class AwsProperties {
	
	InputStream input;
	private String awsMasterSymmetricKey;
	private boolean awsMSKPopulated;
	private String awsAlgorithm;
	private int awsAlgorithmKeyLength;

	private String s3endpoint;
	private String s3region;
	private String s3bucket;
	private String s3prefix;
	private String awsProfilePath;
	private String awsProfileName;

	private int awsRetryCount;
	private String awsLocalKeyDir;
	private boolean okToSaveKeys;
	private String awsLocalDataDir;

	private boolean awsSendEncrypted;
	private boolean awsEnableZip;
	
	/**
	 * 
	 * @param filename location of properties file
	 */
	public AwsProperties(final String filename) {

    	Properties prop = new Properties();
    	
    	try {
    		input = new FileInputStream(filename);
    		if (input == null) {
    	            System.out.println("Can't find properties " + filename);
    		} else {
    			prop.load(input);
    			awsMasterSymmetricKey = prop
    					.getProperty("awsMasterSymmetricKey");
    			awsMSKPopulated = (awsMasterSymmetricKey == null 
    					|| awsMasterSymmetricKey.trim().isEmpty() ? false 
    							: true);
    			awsAlgorithm = prop.getProperty("awsAlgorithm");
    			awsAlgorithmKeyLength = Integer.parseInt(prop
    					.getProperty("awsAlgorithmKeyLength"));
    			s3endpoint = prop.getProperty("s3endpoint");
    			s3region = prop.getProperty("s3region");
    			s3bucket = prop.getProperty("s3bucket");
    			s3prefix = prop.getProperty("s3prefix");
    			awsProfilePath = prop.getProperty("awsProfilePath");
    			
    			// Populate awsProfileName (default AWSProfile)
    			String profileName = prop.getProperty("awsProfileName");
    			if (profileName == null || profileName.trim().isEmpty()) {
    				profileName = "AWSProfile";
    			}
    			awsProfileName = profileName;
    			
    			awsRetryCount = Integer.parseInt(prop
    					.getProperty("awsRetryCount"));
    			
    			awsLocalKeyDir = prop.getProperty("awsLocalKeyDir");
    			okToSaveKeys = (awsLocalKeyDir == null 
    					|| awsLocalKeyDir.trim().isEmpty() ? false 
    							: true);
    			
    			awsLocalDataDir = prop.getProperty("awsLocalDataDir");
    			awsSendEncrypted = ("false".equals((String) 
    					prop.getProperty("awsSendEncrypted")) ? false : true);
    			awsEnableZip = ("false".equals((String) 
    					prop.getProperty("awsEnableZip")) ? false : true); 
    		}
    	} catch (IOException ex) {
    		ex.printStackTrace();
        } finally {
        	if (input != null) {
        		try {
        			input.close();
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
        	}
        }
	}

	/**
	 * 
	 * @return master symmetric key
	 */
	public final String getAwsMasterSymmetricKey() {
		return awsMasterSymmetricKey;
	}


	/**
	 * 
	 * @return algorithm (AES)
	 */
	public final String getAwsAlgorithm() {
		return awsAlgorithm;
	}


	/**
	 * 
	 * @return algorithm length (128,256..)
	 */
	public final int getAwsAlgorithmKeyLength() {
		return awsAlgorithmKeyLength;
	}


	/**
	 * 
	 * @return S3 endpoint
	 */
	public final String getS3endpoint() {
		return s3endpoint;
	}


	/**
	 * 
	 * @return S3 region
	 */
	public final String getS3region() {
		return s3region;
	}


	/**
	 * 
	 * @return S3 bucket
	 */
	public final String getS3bucket() {
		return s3bucket;
	}


	/**
	 * 
	 * @return prefix ("folder") to store on S3
	 */
	public final String getS3prefix() {
		return s3prefix;
	}


	/**
	 * 
	 * @return path of AWS profile info
	 */
	public final String getAwsProfilePath() {
		return awsProfilePath;
	}


	/**
	 * 
	 * @return path of AWS profile info
	 */
	public final String getAwsProfileName() {
		return awsProfileName;
	}

	/**
	 * 
	 * @return number times to retry a process
	 */
	public final int getAwsRetryCount() {
		return awsRetryCount;
	}


	/**
	 * 
	 * @return dir to store keys in
	 */
	public final String getAwsLocalKeyDir() {
		return awsLocalKeyDir;
	}


	/**
	 * 
	 * @return data dir to store temp loads
	 */
	public final String getAwsLocalDataDir() {
		return awsLocalDataDir;
	}


	/**
	 * 
	 * @return whether to send encrypted
	 */
	public final boolean isSendEncrypted() {
		return awsSendEncrypted;
	}


	/**
	 * 
	 * @return is zip enabled
	 */
	public final boolean isEnableZip() {
		return awsEnableZip;
	}


	/**
	 * 
	 * @return is zip enabled
	 */
	public final boolean isAwsMSKPopulated() {
		return awsMSKPopulated;
	}


	/**
	 * 
	 * @return is zip enabled
	 */
	public final boolean isOkToSaveKeys() {
		return okToSaveKeys;
	}
}

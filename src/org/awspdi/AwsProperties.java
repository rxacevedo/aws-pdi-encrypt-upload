package org.awspdi;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 
 * @author Kristofer Ranström
 *
 */
public class AwsProperties {
	
	InputStream input;
	private String masterSymmetricKey;
	private String algorithm;
	private int algorithmKeyLength;

	private String s3endpoint;
	private String s3region;
	private String s3bucket;
	private String s3prefix;
	private String awsProfilePath;

	private int retryCount;
	private String keyDir;
	private String dataDir;

	private boolean sendEncrypted;
	private boolean enableZip;
	
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
    			masterSymmetricKey = prop.getProperty("masterSymmetricKey");
    			algorithm = prop.getProperty("algorithm");
    			algorithmKeyLength = Integer.parseInt(prop
    					.getProperty("algorithmKeyLength"));
    			s3endpoint = prop.getProperty("s3endpoint");
    			s3region = prop.getProperty("s3region");
    			s3bucket = prop.getProperty("s3bucket");
    			s3prefix = prop.getProperty("s3prefix");
    			awsProfilePath = prop.getProperty("awsProfilePath");
    			retryCount = Integer.parseInt(prop.getProperty("retryCount"));
    			keyDir = prop.getProperty("keyDir");
    			dataDir = prop.getProperty("dataDir");
    			sendEncrypted = ("false".equals((String) 
    					prop.getProperty("sendEncrypted")) ? false : true);
    			enableZip = ("false".equals((String) 
    					prop.getProperty("enableZip")) ? false : true); 
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
	public final String getMasterSymmetricKey() {
		return masterSymmetricKey;
	}


	/**
	 * 
	 * @return algorithm (AES)
	 */
	public final String getAlgorithm() {
		return algorithm;
	}


	/**
	 * 
	 * @return algorithm length (128,256..)
	 */
	public final int getAlgorithmKeyLength() {
		return algorithmKeyLength;
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
	 * @return number times to retry a process
	 */
	public final int getRetryCount() {
		return retryCount;
	}


	/**
	 * 
	 * @return dir to store keys in
	 */
	public final String getKeyDir() {
		return keyDir;
	}


	/**
	 * 
	 * @return data dir to store temp loads
	 */
	public final String getDataDir() {
		return dataDir;
	}


	/**
	 * 
	 * @return whether to send encrypted
	 */
	public final boolean isSendEncrypted() {
		return sendEncrypted;
	}


	/**
	 * 
	 * @return is zip enabled
	 */
	public final boolean isEnableZip() {
		return enableZip;
	}
}

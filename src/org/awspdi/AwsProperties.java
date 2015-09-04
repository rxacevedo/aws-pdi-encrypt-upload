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
	private String srcDir;
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
    			srcDir = prop.getProperty("srcDir");
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

	public String getMasterSymmetricKey() {
		return masterSymmetricKey;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public int getAlgorithmKeyLength() {
		return algorithmKeyLength;
	}

	public String getS3endpoint() {
		return s3endpoint;
	}

	public String getS3region() {
		return s3region;
	}

	public String getS3bucket() {
		return s3bucket;
	}

	public String getS3prefix() {
		return s3prefix;
	}

	public String getAwsProfilePath() {
		return awsProfilePath;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public String getSrcDir() {
		return srcDir;
	}

	public String getKeyDir() {
		return keyDir;
	}

	public String getDataDir() {
		return dataDir;
	}

	public boolean isSendEncrypted() {
		return sendEncrypted;
	}

	public boolean isEnableZip() {
		return enableZip;
	}
}

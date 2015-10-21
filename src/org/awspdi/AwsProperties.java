package org.awspdi;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 
 * @author Aenarion
 *
 */
public enum AwsProperties {
	INSTANCE;
	
	String awsMasterSymmetricKey;
	boolean awsMSKPopulated;
	String awsAlgorithm;
	int awsAlgorithmKeyLength;

	String s3endpoint;
	String s3region;
	String s3bucket;
	String s3prefix;
	String awsProfilePath;
	String awsProfileName;

	int awsRetryCount;
	String awsLocalKeyDir;
	boolean okToSaveKeys;
	String awsLocalDataDir;

	boolean awsSendEncrypted;
	boolean awsEnableZip;

	/**
	 * 
	 */
	private AwsProperties() {
	}
	
	/**
	 * 
	 * @param filePath to load properties from 
	 * @return AwsPropertiesSingleton object
	 */
	public AwsProperties loadPropertiesFromPath(
			final String filePath) {
		
		AwsProperties awsProps = AwsProperties.INSTANCE;
		
		Properties prop = new Properties();
		InputStream input = null;
    	
    	try (FileInputStream fos = new FileInputStream(filePath)) {
    		input = fos;
			prop.load(input);
			
			this.awsMasterSymmetricKey = prop
					.getProperty("awsMasterSymmetricKey");
			this.awsMSKPopulated = (awsMasterSymmetricKey == null 
					|| awsMasterSymmetricKey.trim().isEmpty() ? false 
							: true);
			this.awsAlgorithm = prop.getProperty("awsAlgorithm");
			this.awsAlgorithmKeyLength = Integer.parseInt(prop
					.getProperty("awsAlgorithmKeyLength"));
			this.s3endpoint = prop.getProperty("s3endpoint");
			this.s3region = prop.getProperty("s3region");
			this.s3bucket = prop.getProperty("s3bucket");
			this.s3prefix = prop.getProperty("s3prefix");
			this.awsProfilePath = prop.getProperty("awsProfilePath");
			
			// Populate awsProfileName (default AWSProfile)
			String profileName = prop.getProperty("awsProfileName");
			if (profileName == null || profileName.trim().isEmpty()) {
				profileName = "AWSProfile";
			}
			this.awsProfileName = profileName;
			
			this.awsRetryCount = Integer.parseInt(prop
					.getProperty("awsRetryCount"));
			
			this.awsLocalKeyDir = prop.getProperty("awsLocalKeyDir");
			this.okToSaveKeys = (awsLocalKeyDir == null 
					|| awsLocalKeyDir.trim().isEmpty() ? false 
							: true);
			
			this.awsLocalDataDir = prop.getProperty("awsLocalDataDir");
			this.awsSendEncrypted = ("false".equals((String) 
					prop.getProperty("awsSendEncrypted")) ? false : true);
			this.awsEnableZip = ("false".equals((String) 
					prop.getProperty("awsEnableZip")) ? false : true); 
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
		return awsProps;
	}
}

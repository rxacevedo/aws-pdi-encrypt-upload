package org.awspdi;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import com.amazonaws.regions.Region;
// import com.amazonaws.regions.Regions;

/**
 * 
 * @author Kristofer Ranström
 *
 */
public enum AwsProperties {
	INSTANCE;
	
	String awsMasterSymmetricKey;
	boolean awsMSKPopulated;
	String awsAlgorithm;
	int awsAlgorithmKeyLength;

	String s3endpoint;
	Region s3region;
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
	public static AwsProperties loadPropertiesFromPath(
			final String filePath) {
		
		Properties prop = new Properties();
    	
    	try (FileInputStream fos = new FileInputStream(filePath)) {
			prop.load(fos);
			
			INSTANCE.awsMasterSymmetricKey = prop
					.getProperty("awsMasterSymmetricKey");
			INSTANCE.awsMSKPopulated = !(INSTANCE.awsMasterSymmetricKey == null 
					|| INSTANCE.awsMasterSymmetricKey.trim().isEmpty());
			INSTANCE.awsAlgorithm = prop.getProperty("awsAlgorithm");
			INSTANCE.awsAlgorithmKeyLength = Integer.parseInt(prop
					.getProperty("awsAlgorithmKeyLength"));
			INSTANCE.s3endpoint = prop.getProperty("s3endpoint");
			
//			Regions region = Regions.valueOf(prop.getProperty("s3region"));
//        	this.s3region = Region.getRegion(region);
			
			INSTANCE.s3bucket = prop.getProperty("s3bucket");
			INSTANCE.s3prefix = prop.getProperty("s3prefix");
			INSTANCE.awsProfilePath = prop.getProperty("awsProfilePath");
			
			// Populate awsProfileName (default AWSProfile)
			String profileName = prop.getProperty("awsProfileName");
			if (profileName == null || profileName.trim().isEmpty()) {
				profileName = "AWSProfile";
			}
			INSTANCE.awsProfileName = profileName;
			
			INSTANCE.awsRetryCount = Integer.parseInt(prop
					.getProperty("awsRetryCount"));
			
			INSTANCE.awsLocalKeyDir = prop.getProperty("awsLocalKeyDir");
			INSTANCE.okToSaveKeys = !(INSTANCE.awsLocalKeyDir == null 
					|| INSTANCE.awsLocalKeyDir.trim().isEmpty());
			
			INSTANCE.awsLocalDataDir = prop.getProperty("awsLocalDataDir");
			INSTANCE.awsSendEncrypted = !("false".equals((String) 
					prop.getProperty("awsSendEncrypted")));
			INSTANCE.awsEnableZip = !("false".equals((String) 
					prop.getProperty("awsEnableZip"))); 
    	} catch (IOException ex) {
    		ex.printStackTrace();
        }
		return INSTANCE;
	}
}

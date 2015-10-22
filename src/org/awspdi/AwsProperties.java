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
		
		AwsProperties awsProps = INSTANCE;
		
		Properties prop = new Properties();
    	
    	try (FileInputStream fos = new FileInputStream(filePath)) {
			prop.load(fos);
			
			awsProps.awsMasterSymmetricKey = prop
					.getProperty("awsMasterSymmetricKey");
			awsProps.awsMSKPopulated = !(awsProps.awsMasterSymmetricKey == null 
					|| awsProps.awsMasterSymmetricKey.trim().isEmpty());
			awsProps.awsAlgorithm = prop.getProperty("awsAlgorithm");
			awsProps.awsAlgorithmKeyLength = Integer.parseInt(prop
					.getProperty("awsAlgorithmKeyLength"));
			awsProps.s3endpoint = prop.getProperty("s3endpoint");
			
//			Regions region = Regions.valueOf(prop.getProperty("s3region"));
//        	this.s3region = Region.getRegion(region);
			
			awsProps.s3bucket = prop.getProperty("s3bucket");
			awsProps.s3prefix = prop.getProperty("s3prefix");
			awsProps.awsProfilePath = prop.getProperty("awsProfilePath");
			
			// Populate awsProfileName (default AWSProfile)
			String profileName = prop.getProperty("awsProfileName");
			if (profileName == null || profileName.trim().isEmpty()) {
				profileName = "AWSProfile";
			}
			awsProps.awsProfileName = profileName;
			
			awsProps.awsRetryCount = Integer.parseInt(prop
					.getProperty("awsRetryCount"));
			
			awsProps.awsLocalKeyDir = prop.getProperty("awsLocalKeyDir");
			awsProps.okToSaveKeys = !(awsProps.awsLocalKeyDir == null 
					|| awsProps.awsLocalKeyDir.trim().isEmpty());
			
			awsProps.awsLocalDataDir = prop.getProperty("awsLocalDataDir");
			awsProps.awsSendEncrypted = !("false".equals((String) 
					prop.getProperty("awsSendEncrypted")));
			awsProps.awsEnableZip = !("false".equals((String) 
					prop.getProperty("awsEnableZip"))); 
    	} catch (IOException ex) {
    		ex.printStackTrace();
        }
		return awsProps;
	}
}

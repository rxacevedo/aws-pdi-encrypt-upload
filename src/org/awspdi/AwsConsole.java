package org.awspdi;

import javax.crypto.SecretKey;
import org.awspdi.encrypt.GenerateSymmetricMasterKey;

public class AwsConsole {
	// TODO Make argument
	public final static String fileName = "DimDate.csv";
	public final static String propertiesPath = "aws.properties";
	public final static String profilePath = "aws.profile";

	public static void main(String[] args) {	    
		
		// Get Properties used in the process
		AwsProperties awsProperties = 
				new AwsProperties(propertiesPath);
		
		// Initialize a  new key to be used
		GenerateSymmetricMasterKey newKey = new GenerateSymmetricMasterKey(awsProperties.getKeyDir(),"DimDate"); 

		// Gzip file for smaller footprint. Redshift can handle this later
        String gzipFileName = fileName + ".gzip";
        
    	CompressFileGzip gZipFile = new CompressFileGzip();
		gZipFile.gzipFile(awsProperties.dataDir + "/" + fileName, 
				awsProperties.dataDir + "/" + gzipFileName);
		
		SecretKey mySymmetricKey = newKey.loadSymmetricAESKey("AES");
//        S3Service.uploadToS3(awsProperties.s3bucket, awsProperties.dataDir + "/" + gzipFileName,
//        		mySymmetricKey, propertiesLocation);
        S3Service.uploadToS3Unencrypted(awsProperties.s3bucket, awsProperties.dataDir + "/" + gzipFileName, profilePath);
        
		// TODO Get list of files for encryption
		// TODO Get AWS connection and establish connection
		// TODO Upload encrypted files as part of connection
		
	}
}
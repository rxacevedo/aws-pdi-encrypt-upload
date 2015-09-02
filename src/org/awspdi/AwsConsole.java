package org.awspdi;

import javax.crypto.SecretKey;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.awspdi.encrypt.GenerateSymmetricMasterKey;

public class AwsConsole {
	private static String fileName;
	private static String propertiesPath;

	public static void main(String[] args) {  
		
		// Populate input args and do not execute if required arguments aren't populated
		if(checkAndPopulateInputArguments(args)) {
			// Get Properties used in the process
			AwsProperties awsProperties = 
					new AwsProperties(propertiesPath);

			// Gzip file for smaller footprint. Redshift can handle this later
	        String gzipFileName = fileName + ".gzip";
	        
	    	CompressFileGzip gZipFile = new CompressFileGzip();
			gZipFile.gzipFile(awsProperties.dataDir + "/" + fileName, 
					awsProperties.dataDir + "/" + gzipFileName);
			
			if (awsProperties.sendEncrypted) {
				if (awsProperties.useKMS) {
					S3Service.uploadWithKMSKey(awsProperties.s3bucket, awsProperties.dataDir + "/" + gzipFileName, 
			        		awsProperties.awsProfilePath, awsProperties.kms_cmk_id);
				} else {
					// Initialize a  new key to be used
					GenerateSymmetricMasterKey newKey = new GenerateSymmetricMasterKey(awsProperties,"DimDate"); 
					SecretKey mySymmetricKey = newKey.loadSymmetricAESKey("AES");
			        S3Service.uploadToS3(awsProperties.s3bucket, awsProperties.dataDir + "/" + gzipFileName,
			        		mySymmetricKey, awsProperties.awsProfilePath);
				}
			} else {
				S3Service.uploadToS3Unencrypted(awsProperties.s3bucket, awsProperties.dataDir + "/" + gzipFileName, 
						awsProperties.awsProfilePath);
			}
		} else {
			System.out.println("All required arguments were not populated. Please try again.");
		}
		
	}
	
	private static boolean checkAndPopulateInputArguments(String [] args) {
		// create Options object
		Options options = new Options();
		options.addOption("fileName", true, "Name of the file to be encrypyed and uploaded");
		options.addOption("propertiesPath", true, "Path to the properties file");
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse( options, args);
			fileName = cmd.getOptionValue("fileName");
			propertiesPath = cmd.getOptionValue("propertiesPath");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return (fileName == null || propertiesPath == null ? false : true);
	}
}
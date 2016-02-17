package org.awspdi;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.amazonaws.services.s3.model.StaticEncryptionMaterialsProvider;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Kristofer Ranström
 */
public class AwsConsole {
	private static boolean ENV_CREDS = false;
	/**
	 * 
	 */
	private static String fileOrDirectory;
	
	/**
	 * 
	 */
	private static String propertiesPath;
	private static AWSCredentials awsCredentials;
	private static AwsProperties awsProperties;

	/**
	 * Calls to check the input arguments from command line
	 * before determining whether or not to execute
	 * the uploader.
	 * 
	 * @param args input arguments
	 */
	public static void main(String[] args) {

	    // args = new String[4];
		// args[0] = "-fileOrDirectory";
		// args[1] = "/vagrant/pdi/files";
		// args[2] = "-propertiesPath";
		// args[3] = "/vagrant/pdi/aws.properties";

		// Populate input args and do not execute if required arguments 
		// aren't populated
		if (checkAndPopulateInputArguments(args)) {

			// Get Properties used in the process
            try {

			AwsUploadToS3 s3Upload = new AwsUploadToS3(AwsProperties
					.loadPropertiesFromPath(propertiesPath), 
					fileOrDirectory);
			
                s3Upload.uploadToS3Manager();
			
			System.out.println("Upload Complete");

            } catch (Exception e) {
                System.out.println("Upload Failed");
                e.printStackTrace();
            }

		} else {
			System.out.println("All required arguments were not populated. "
					+ "Please try again.");
		}
		
	}
	
	/**
	 * This checks the input arguments.
	 * 
	 * @param args 
	 * @return confirm arguments populated
	 */
    private static boolean checkAndPopulateInputArguments(final String[] args) {

		// create Options object
		Options options = new Options();

		options.addOption("fileOrDirectory", true,
				"Name of the file or directory to be (encrypted and) uploaded");

		options.addOption("propertiesPath", true,
				"Path to the properties file");
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
			fileOrDirectory = cmd.getOptionValue("fileOrDirectory");
			propertiesPath = cmd.getOptionValue("propertiesPath");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return !(fileOrDirectory == null || propertiesPath == null);
	}
}
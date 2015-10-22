package org.awspdi;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * 
 * @author Kristofer Ranström
 *
 */
public class AwsConsole {
	/**
	 * 
	 */
	private static String fileOrDirectory;
	
	/**
	 * 
	 */
	private static String propertiesPath;

	/**
	 * Calls to check the input arguments from command line
	 * before determining whether or not to execute
	 * the uploader.
	 * 
	 * @param args input arguments
	 */
	public static void main(final String[] args) {
		// Populate input args and do not execute if required arguments 
		// aren't populated
		if (checkAndPopulateInputArguments(args)) {
			// Get Properties used in the process
			AwsUploadToS3 s3Upload = new AwsUploadToS3(AwsProperties
					.loadPropertiesFromPath(propertiesPath), 
					fileOrDirectory);
			s3Upload.uploadToS3Manager();
			
			
			System.out.println("Upload Complete");
		} else {
			System.out.println("All required arguments were not populated. "
					+ "Please try again.");
		}
		
	}
	
	/**
	 * 
	 * This checks the input arguments.
	 * 
	 * @param args 
	 * @return confirm arguments populated
	 */
	private static boolean checkAndPopulateInputArguments(
			final String [] args) {
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
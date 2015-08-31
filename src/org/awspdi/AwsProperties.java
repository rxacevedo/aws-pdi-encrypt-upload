package org.awspdi;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AwsProperties {
	
	InputStream input;
	String accessKey;
	String secretAccessKey;
	String s3bucket;
	String keyDir;
	String dataDir;
	
	public AwsProperties(String filename) {

    	Properties prop = new Properties();
    	
    	try {
    		input = new FileInputStream(filename);
    		if (input==null) {
    	            System.out.println(filename + " can't find ");
    		} else {
    			prop.load(input);
                s3bucket = prop.getProperty("s3bucket");
                keyDir = prop.getProperty("keyDir");
                dataDir = prop.getProperty("dataDir");
    		}
    	} catch (IOException ex) {
    		ex.printStackTrace();
        } finally {
        	if (input!=null) {
        		try {
        			input.close();
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
        	}
        }
	}

	public String getAccessKey() {
		return accessKey;
	}

	public String getSecretAccessKey() {
		return secretAccessKey;
	}

	public String getS3bucket() {
		return s3bucket;
	}

	public String getKeyDir() {
		return keyDir;
	}

	public String getDataDir() {
		return dataDir;
	}
}

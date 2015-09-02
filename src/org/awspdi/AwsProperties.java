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
	String kms_cmk_id;
	String awsProfilePath;
	String algorithm;
	int algorithmKeyLength;
	boolean useKMS;
	boolean sendEncrypted;
	
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
                kms_cmk_id = prop.getProperty("kms_cmk_id");
                awsProfilePath = prop.getProperty("awsProfilePath");
                algorithm = prop.getProperty("algorithm");
                algorithmKeyLength = Integer.parseInt(prop.getProperty("algorithmKeyLength"));
                
                // Defaults to use KMS
                useKMS = ("true".equals((String) prop.getProperty("useKMS")) ? true : false);
                // Defaults to send encrypted
                sendEncrypted = ("false".equals((String) prop.getProperty("sendEncrypted")) ? false : true);
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

	public InputStream getInput() {
		return input;
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

	public String getKms_cmk_id() {
		return kms_cmk_id;
	}

	public String getAwsProfilePath() {
		return awsProfilePath;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public int getAlgorithmKeyLength() {
		return algorithmKeyLength;
	}

	public boolean isUseKMS() {
		return useKMS;
	}
}

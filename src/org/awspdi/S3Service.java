package org.awspdi;

import java.io.File;
//import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.awspdi.encrypt.GenerateSymmetricMasterKey;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
//import com.amazonaws.regions.Region;
//import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.amazonaws.services.s3.model.PutObjectRequest;

/**
 * 
 * The S3 service supports both encrypted and unencrypted uploads.
 * Most of this is your typical AWS SDK stuff...
 * 
 * Many thanks to: http://yoshidashingo.hatenablog.com/entry/2014/08/10/193631
 * for some valuable suggestions/additions.
 * 
 * @author Kristofer Ranström
 *
 */
public final class S3Service {
	
	private static AwsProperties awsProperties;
	private static AWSCredentials awsCredentials;
	private static AmazonS3EncryptionClient awsEncryptionClient;
	private static AmazonS3 awsClient;
	private static EncryptionMaterials materials;
	private static GenerateSymmetricMasterKey newKey;
    
    /**
     * 
     * @param awsProps props
     */
    S3Service(final AwsProperties awsProps) {

    	awsProperties = awsProps;
        try {
        	ProfilesConfigFile profileConfig = new ProfilesConfigFile(
        			awsProperties.awsProfilePath);
            awsCredentials = new ProfileCredentialsProvider(
            		profileConfig, awsProperties.awsProfileName)
            		.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential "
            		+ "profiles file.", e);
        }

        if (awsProperties.awsSendEncrypted) {
        	
        	// If key is not populated we need to generate one
        	String msk = awsProperties.awsMasterSymmetricKey;
        	if (!awsProperties.awsMSKPopulated) {
        		newKey = new GenerateSymmetricMasterKey(
        				awsProperties.awsLocalKeyDir, "Key.key", 
        				awsProperties.awsAlgorithm, 
        				awsProperties.awsAlgorithmKeyLength);
        		msk = newKey.getSymmetricMasterKey();
        	}
        	
        	SecretKey symmetricKey = new SecretKeySpec(
                        Base64.decodeBase64(msk.getBytes()), 
                        awsProperties.awsAlgorithm);

            materials = new EncryptionMaterials(symmetricKey);        

            awsEncryptionClient = new AmazonS3EncryptionClient(awsCredentials, 
            		materials);
            awsEncryptionClient.setEndpoint(awsProperties.s3endpoint);        	
        } else {
        	awsClient = new AmazonS3Client(awsCredentials);
        	awsClient.setEndpoint(awsProperties.s3endpoint);
//          TODO Support regional assignment
//        	Region usWest2 = Region.getRegion(Regions.US_WEST_2);
//        	awsClient.setRegion(usWest2);
        }
    }
    
    /**
     * 
     * @param fileToUpload 
     */
    public void uploadToS3Unencrypted(final File fileToUpload) {

    	// TODO: add support for UUID random names
        // String objectKey = UUID.randomUUID().toString();

        try {
            System.out.println("Uploading a new object to S3 object '"
                    + awsProperties.s3prefix + "' from file " 
            		+ fileToUpload.getName());

            System.out.println("Uploading a new object to S3 from a file\n");
            awsClient.putObject(new PutObjectRequest(awsProperties.s3bucket, 
            		awsProperties.s3prefix + "/" + fileToUpload.getName(), 
            		fileToUpload));

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means "
            		+ "your request made it to Amazon S3, but was rejected "
            		+ "with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means "
            		+ "the client encountered a serious internal problem while "
            		+ "trying to communicate with S3, such as not being able "
            		+ "to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }
    
    /**
     * 
     * @param fileToUpload 
     */
    public void uploadToS3Encrypted(final File fileToUpload) {        

        try {
            System.out.println("Uploading a new object to S3 object '"
                    + awsProperties.s3prefix + "' from file " 
            		+ fileToUpload.getName());
            String key = awsProperties.s3prefix + "/" + fileToUpload.getName();
            awsEncryptionClient.putObject(new PutObjectRequest(
            		awsProperties.s3bucket, key, fileToUpload));

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means "
            		+ "your request made it to Amazon S3, but was rejected "
            		+ "with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());

            throw ase;
        } catch (AmazonClientException ace) {
            System.out
                    .println("Caught an AmazonClientException, which means the "
                    		+ "client encountered a serious internal problem "
                    		+ "while trying to communicate with S3, such as "
                    		+ "not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
            throw ace;
        }
    	
    }
    
    /**
     * 
     * @return newly generated key 
     */
    public GenerateSymmetricMasterKey getNewKey() {
    	return newKey;
    }
}
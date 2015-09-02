package org.awspdi;
import java.io.File;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.awspdi.encrypt.GenerateSymmetricMasterKey;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.model.CryptoConfiguration;
import com.amazonaws.services.s3.model.CryptoMode;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.amazonaws.services.s3.model.KMSEncryptionMaterialsProvider;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.StaticEncryptionMaterialsProvider;

/**
 * This sample demonstrates how to make basic requests to Amazon S3 using the
 * AWS SDK for Java.
 * <p>
 * <b>Prerequisites:</b> You must have a valid Amazon Web Services developer
 * account, and be signed up to use Amazon S3. For more information on Amazon
 * S3, see http://aws.amazon.com/s3.
 * <p>
 * Fill in your AWS access credentials in the provided credentials file
 * template, and be sure to move the file to the default location
 * (/Users/Aenarion/.aws/credentials) where the sample code will load the credentials from.
 * <p>
 * <b>WARNING:</b> To avoid accidental leakage of your credentials, DO NOT keep
 * the credentials file in your source directory.
 *
 * http://aws.amazon.com/security-credentials
 */
public class S3Service {
	private static AmazonS3EncryptionClient encryptionClient;
    private static final String objectKey = UUID.randomUUID().toString();
    private static final Region usWest2 = Region.getRegion(Regions.US_WEST_2);

    public static void uploadToS3(String bucketName, String filePath, SecretKey mySymmetricKey, 
    		String profilePath) {

        /*
         * The ProfileCredentialsProvider will return credentials from properties file
         */
        AWSCredentials credentials = setupAWSCredentials(profilePath);
        //AmazonS3 s3 = new AmazonS3Client(credentials);
        //s3.setRegion(usWest2);
        
        EncryptionMaterials encryptionMaterials = new EncryptionMaterials(
                mySymmetricKey);
        
        // CryptoConfiguration requires: Java Cryptography Extension (JCE) Unlimited 
        encryptionClient = new AmazonS3EncryptionClient(
        		credentials,
                new StaticEncryptionMaterialsProvider(encryptionMaterials),
                new CryptoConfiguration(CryptoMode.AuthenticatedEncryption));
        encryptionClient.setRegion(usWest2);

        try {

        	File fileToUpload = new File(filePath);
            /*
             * Upload an object to your bucket - You can easily upload a file to
             * S3, or upload directly an InputStream if you know the length of
             * the data in the stream. You can also specify your own metadata
             * when uploading to S3, which allows you set a variety of options
             * like content-type and content-encoding, plus additional metadata
             * specific to your applications.
             */
            System.out.println("Uploading a new object to S3 from a file\n");
            // s3.putObject(new PutObjectRequest(bucketName, objectKey, fileToUpload));
            
            ObjectMetadata metadata = new ObjectMetadata();
            // x-amz-meta-x-amz-iv
//            metadata.addUserMetadata("x-amz-iv", "");
            // x-amz-meta-x-amz-key
            metadata.addUserMetadata("x-amz-key", 
            		GenerateSymmetricMasterKey.friendlyKey(mySymmetricKey));
            
            encryptionClient.putObject(new PutObjectRequest(bucketName, objectKey,
	        		fileToUpload).withMetadata(metadata));

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }

    public static void uploadToS3Unencrypted(String bucketName, String filePath, String profilePath) {

        /*
         * The ProfileCredentialsProvider will return your [AWSPOC]
         * credential profile by reading from the credentials file located at
         * (/Users/<Username>/.aws/credentials).
         */
        AWSCredentials credentials = setupAWSCredentials(profilePath);

        AmazonS3 s3 = new AmazonS3Client(credentials);
        s3.setRegion(usWest2);

        try {

        	File fileToUpload = new File(filePath);
            /*
             * Upload an object to your bucket - You can easily upload a file to
             * S3, or upload directly an InputStream if you know the length of
             * the data in the stream. You can also specify your own metadata
             * when uploading to S3, which allows you set a variety of options
             * like content-type and content-encoding, plus additional metadata
             * specific to your applications.
             */
            System.out.println("Uploading a new object to S3 from a file\n");
            s3.putObject(new PutObjectRequest(bucketName, objectKey, fileToUpload));

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }
    
    private static AWSCredentials setupAWSCredentials(String profilePath) {
    	AWSCredentials credentials = null;
        try {
        	ProfilesConfigFile profileConfig = new ProfilesConfigFile(profilePath);
            credentials = new ProfileCredentialsProvider(profileConfig ,"AWSProfile").getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file.",
                    e);
        }
    	return credentials;
    }
    
    public static void uploadWithKMSKey(String bucketName, String filePath, 
    		String profilePath, String kms_cmk_id) {

        AWSCredentials credentials = setupAWSCredentials(profilePath);
        KMSEncryptionMaterialsProvider materialProvider = new KMSEncryptionMaterialsProvider(kms_cmk_id);
    	File fileToUpload = new File(filePath);

        encryptionClient = new AmazonS3EncryptionClient(
        		credentials,
        		materialProvider,
                new CryptoConfiguration().withKmsRegion(Regions.US_EAST_1));
        encryptionClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        
        // Upload object using the encryption client.
        encryptionClient.putObject(new PutObjectRequest(bucketName, objectKey, fileToUpload));
        
        System.out.println("Upload complete!");
    }
}
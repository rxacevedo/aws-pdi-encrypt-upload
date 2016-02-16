package org.awspdi;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
//import java.util.UUID;
import java.util.List;
import java.security.MessageDigest;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.awspdi.encrypt.GenerateSymmetricMasterKey;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.StaticEncryptionMaterialsProvider;
import com.amazonaws.services.s3.model.UploadPartRequest;

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
  private static boolean ENV_CREDS;
	
	/**
	 *  Multi part upload size.
	 */
	private static final long MULTI_PART_UPLOAD_SIZE = 5242880;
	
	/**
	 *  Multi part upload size.
	 */
	private static final long ENCRYPTED_MULTI_PART_UPLOAD_SIZE = 5242880; // 
    
    /**
     * 
     * @param awsProps props
     */
    S3Service(final AwsProperties awsProps) {
    	
    	awsProperties = awsProps;

    if (!(null == System.getenv("AWS_ACCESS_KEY_ID") &&
        null == System.getenv("AWS_SECRET_ACCESS_KEY"))) {

      System.out.println("Loading credentials from env...");
      ENV_CREDS = true;

    } else {

      System.out.println("Loading credentials from file...");
      ENV_CREDS = false;

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

      StaticEncryptionMaterialsProvider matsProvider = new StaticEncryptionMaterialsProvider(materials);
      awsEncryptionClient = ENV_CREDS ? new AmazonS3EncryptionClient(new EnvironmentVariableCredentialsProvider(), matsProvider)
        : new AmazonS3EncryptionClient(awsCredentials, materials);

            awsEncryptionClient.setEndpoint(awsProperties.s3endpoint);
            // awsEncryptionClient.setRegion(awsProperties.s3region);   	
        } else {
      System.out.println("Sending file unencrypted...");
      awsClient = ENV_CREDS ? new AmazonS3Client(new EnvironmentVariableCredentialsProvider())
        : new AmazonS3Client(awsCredentials);
        	awsClient.setEndpoint(awsProperties.s3endpoint);
        	// awsClient.setRegion(awsProperties.s3region);
        }
    }
    
    /**
     * @param fileToUpload the file to be uploaded
     */
    public void uploadMultiPartToS3Unencrypted(final File fileToUpload) {

        String key = awsProperties.s3prefix + "/" + fileToUpload.getName();

        // Create a list of UploadPartResponse objects. You get one of these
        // for each part upload.
        List<PartETag> partETags = new ArrayList<PartETag>();

        // Step 1: Initialize.
        InitiateMultipartUploadRequest initRequest = new 
             InitiateMultipartUploadRequest(awsProperties.s3bucket, key);
        InitiateMultipartUploadResult initResponse = 
        		awsClient.initiateMultipartUpload(initRequest);

        long contentLength = fileToUpload.length();
        long partSize = MULTI_PART_UPLOAD_SIZE;

        try {
            // Step 2: Upload parts.
            long filePosition = 0;
            for (int i = 1; filePosition < contentLength; i++) {
                // Last part can be less than 5 MB. Adjust part size.
            	partSize = Math.min(partSize, (contentLength - filePosition));
            	
                // Create request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest()
                    .withBucketName(awsProperties.s3bucket).withKey(key)
                    .withUploadId(initResponse.getUploadId()).withPartNumber(i)
                    .withFileOffset(filePosition)
                    .withFile(fileToUpload)
                    .withPartSize(partSize);

                // Upload part and add response to our list.
                partETags.add(
                		awsClient.uploadPart(uploadRequest).getPartETag());

                filePosition += partSize;
            }

            // Step 3: Complete.
            CompleteMultipartUploadRequest compRequest = new 
                         CompleteMultipartUploadRequest(
                        		 awsProperties.s3bucket, 
                        		 key, initResponse.getUploadId(), 
                                    partETags);

            awsClient.completeMultipartUpload(compRequest);
        } catch (Exception e) {
        	awsClient.abortMultipartUpload(new AbortMultipartUploadRequest(
        			awsProperties.s3bucket, key, 
        			initResponse.getUploadId()));
      e.printStackTrace();
        }
    }
    
    /**
     * @param fileToUpload the file to be uploaded
     */
    public void uploadMultiPartToS3Encrypted(final File fileToUpload) {

        String key = awsProperties.s3prefix + "/" + fileToUpload.getName();

        // Create a list of UploadPartResponse objects. You get one of these
        // for each part upload.
        List<PartETag> partETags = new ArrayList<PartETag>();

        // Step 1: Initialize.
        InitiateMultipartUploadRequest initRequest = new 
             InitiateMultipartUploadRequest(awsProperties.s3bucket, key);
        InitiateMultipartUploadResult initResponse = 
        		awsEncryptionClient.initiateMultipartUpload(initRequest);

        long contentLength = fileToUpload.length();
        long partSize = ENCRYPTED_MULTI_PART_UPLOAD_SIZE;

        try {
            // Step 2: Upload parts.
            long filePosition = 0;
            for (int i = 1; filePosition < contentLength; i++) {
                // Last part can be less than 5 MB. Adjust part size.
            	partSize = Math.min(partSize, (contentLength - filePosition));
            	boolean isLastPart = 
            			ENCRYPTED_MULTI_PART_UPLOAD_SIZE > partSize;
            	
                // Create request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest()
                    .withBucketName(awsProperties.s3bucket).withKey(key)
                    .withUploadId(initResponse.getUploadId()).withPartNumber(i)
                    .withFileOffset(filePosition)
                    .withFile(fileToUpload)
                    .withPartSize(partSize)
                    .withLastPart(isLastPart);

                // Upload part and add response to our list.
                partETags.add(awsEncryptionClient.uploadPart(uploadRequest)
                		.getPartETag());

                filePosition += partSize;
            }

            // Step 3: Complete.
            CompleteMultipartUploadRequest compRequest = 
            		new CompleteMultipartUploadRequest(awsProperties.s3bucket,
            				key, initResponse.getUploadId(), partETags);

            awsEncryptionClient.completeMultipartUpload(compRequest);
        } catch (Exception e) {
        	awsEncryptionClient.abortMultipartUpload(
        			new AbortMultipartUploadRequest(
        					awsProperties.s3bucket, key, 
        			initResponse.getUploadId()));
        	System.out.println(e.getMessage());
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
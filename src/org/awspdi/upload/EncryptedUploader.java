package org.awspdi.upload;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.codec.binary.Base64;
import org.awspdi.AwsProperties;
import org.awspdi.encrypt.GenerateSymmetricMasterKey;
import org.awspdi.util.UploaderConst;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vagrant on 2/16/16.
 */
public class EncryptedUploader implements IUploader {

    private static final String LOG_UPLOAD_BEGIN =
            "****BEGIN UPLOADED FILES & KEYS******";
    private static final String LOG_UPLOAD_END =
            "****END UPLOADED FILES & KEYS******";

    private AmazonS3Client client;
    private AwsProperties uploadContext;
    private File uploadFile;
    private GenerateSymmetricMasterKey newKey;

    public EncryptedUploader(AmazonS3Client client) {
       this.client = client;
    }

    @Override
    public void upload() throws Exception {

        String key = uploadContext.s3prefix + "/" + uploadFile.getName();

        // Create a list of UploadPartResponse objects. You get one of these
        // for each part upload.
        List<PartETag> partETags = new ArrayList<PartETag>();

        // Step 1: Initialize.
        InitiateMultipartUploadRequest initRequest = new
                InitiateMultipartUploadRequest(uploadContext.s3bucket, key);
        InitiateMultipartUploadResult initResponse =
                client.initiateMultipartUpload(initRequest);

        long contentLength = uploadFile.length();
        long partSize = UploaderConst.ENCRYPTED.getChunkSize();

        try {
            // Step 2: Upload parts.
            long filePosition = 0;
            for (int i = 1; filePosition < contentLength; i++) {
                // Last part can be less than 5 MB. Adjust part size.
                partSize = Math.min(partSize, (contentLength - filePosition));
                boolean isLastPart =
                        UploaderConst.ENCRYPTED.getChunkSize() > partSize;

                // Create request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest()
                        .withBucketName(uploadContext.s3bucket).withKey(key)
                        .withUploadId(initResponse.getUploadId()).withPartNumber(i)
                        .withFileOffset(filePosition)
                        .withFile(uploadFile)
                        .withPartSize(partSize)
                        .withLastPart(isLastPart);

                // Upload part and add response to our list.
                partETags.add(client.uploadPart(uploadRequest)
                        .getPartETag());

                filePosition += partSize;
                printOrSaveKey(uploadFile.getName());
            }

            // Step 3: Complete.
            CompleteMultipartUploadRequest compRequest =
                    new CompleteMultipartUploadRequest(uploadContext.s3bucket,
                            key, initResponse.getUploadId(), partETags);

            client.completeMultipartUpload(compRequest);

            printOrSaveKey(uploadFile.getName());

        } catch (Exception e) {
            client.abortMultipartUpload(
                    new AbortMultipartUploadRequest(
                            uploadContext.s3bucket, key,
                            initResponse.getUploadId()));
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void setUploadContent(String filePath) {
        File file = Paths.get(filePath).toFile();
        setUploadContent(file);
    }

    public void setUploadContent(File file) {
        this.uploadFile = file;
    }

    @Override
    public File getUploadContent() {
        return null;
    }

    @Override
    public void setUploadContext(AwsProperties properties) {
       this.uploadContext = properties;
    }

    /**
     *
     * @return newly generated key
     */
    public GenerateSymmetricMasterKey getNewKey() {

        // If key is not populated we need to generate one
        String msk = uploadContext.awsMasterSymmetricKey;
        if (!uploadContext.awsMSKPopulated) {
            newKey = new GenerateSymmetricMasterKey(
                    uploadContext.awsLocalKeyDir, "Key.key",
                    uploadContext.awsAlgorithm,
                    uploadContext.awsAlgorithmKeyLength);
            msk = newKey.getSymmetricMasterKey();
        }

        SecretKey symmetricKey = new SecretKeySpec(
                Base64.decodeBase64(msk.getBytes()),
                uploadContext.awsAlgorithm);

        return newKey;

    }

    /**
     *  Save or print key depending on whether key directory is
     *  specified.
     *
     * @param fileName for saving keyfile
     */
    private void printOrSaveKey(final String fileName) {
        // If MSK does not exist we need to save it
        if (!uploadContext.awsMSKPopulated
                && uploadContext.okToSaveKeys) {
            getNewKey().saveSingleSymmetricKey(
                    fileName + ".key", false);
        } else {
            System.out.println(LOG_UPLOAD_BEGIN + "\n"
                    + "File: " + fileName + " Key: "
                    + getNewKey().getSymmetricMasterKey()
                    + "\n" + LOG_UPLOAD_END);
        }
    }

    /**
     *  Method saves newly generated keys as a CSV
     *  file into the local key directory.
     *  If no directory is defined it will simply
     *  output the contents to stdout.
     *
     * @param keyMap key map to save
     */
    public static void saveOrOutputKeyMap(final HashMap<String, String> keyMap, AwsProperties uploadContext) {

        if (!keyMap.isEmpty()) {
            if (uploadContext.okToSaveKeys) {
                try {
                    FileWriter writer = new FileWriter(uploadContext
                            .awsLocalKeyDir + "/keyMap.csv");
                    writer.append("Filename");
                    writer.append(',');
                    writer.append("MSK");
                    writer.append('\n');

                    for (Map.Entry<String, String> entry : keyMap.entrySet()) {
                        writer.append(entry.getKey());
                        writer.append(',');
                        writer.append(entry.getValue());
                        writer.append('\n');
                    }

                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println(LOG_UPLOAD_BEGIN);
                for (Map.Entry<String, String> entry : keyMap.entrySet()) {
                    System.out.println("File: " + entry.getKey()
                            + " | Key: " + entry.getValue());
                }
                System.out.println(LOG_UPLOAD_END);
            }
        } else {
            System.out.println("Keymap is empty, nothing to write");
        }
    }

}

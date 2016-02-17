package org.awspdi.upload;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.awspdi.AwsProperties;
import org.awspdi.util.UploaderConst;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by roberto on 2/16/16.
 */
public class Uploader implements IUploader {

    private AmazonS3Client client;
    private AwsProperties uploadContext;
    private File uploadFile;

    public Uploader(AmazonS3Client client) {
       this.client = client;
    }

    @Override
    public void upload() throws Exception {

        String key = String.format("%s/%s", uploadContext.s3prefix, uploadFile.getName());

        // Create a list of UploadPartResponse objects. You get one of these
        // for each part upload.
        List<PartETag> partETags = new ArrayList<PartETag>();

        // Step 1: Initialize.
        InitiateMultipartUploadRequest initRequest = new
                InitiateMultipartUploadRequest(uploadContext.s3bucket, key);

        InitiateMultipartUploadResult initResponse =
                client.initiateMultipartUpload(initRequest);

        long contentLength = uploadFile.length();
        long partSize = UploaderConst.UNENCRYPTED.getChunkSize();

        try {
            // Step 2: Upload parts.
            long filePosition = 0;
            for (int i = 1; filePosition < contentLength; i++) {
                // Last part can be less than 5 MB. Adjust part size.
                partSize = Math.min(partSize, (contentLength - filePosition));

                // Create request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest()
                        .withBucketName(uploadContext.s3bucket).withKey(key)
                        .withUploadId(initResponse.getUploadId()).withPartNumber(i)
                        .withFileOffset(filePosition)
                        .withFile(uploadFile)
                        .withPartSize(partSize);

                // Upload part and add response to our list.
                partETags.add(
                        client.uploadPart(uploadRequest).getPartETag());

                filePosition += partSize;
            }

            // Step 3: Complete.
            CompleteMultipartUploadRequest compRequest = new
                    CompleteMultipartUploadRequest(
                    uploadContext.s3bucket,
                    key, initResponse.getUploadId(),
                    partETags);

            client.completeMultipartUpload(compRequest);
        } catch (Exception e) {
            client.abortMultipartUpload(new AbortMultipartUploadRequest(
                    uploadContext.s3bucket, key,
                    initResponse.getUploadId()));
            e.printStackTrace();
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
        return uploadFile;
    }

    @Override
    public void setUploadContext(AwsProperties properties) {
        this.uploadContext = properties;
    }

}

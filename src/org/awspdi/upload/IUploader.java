package org.awspdi.upload;

import com.amazonaws.services.s3.AmazonS3Client;
import org.awspdi.AwsProperties;

import java.io.File;

/**
 * Created by vagrant on 2/16/16.
 */
public interface IUploader {

    /***
     * Runs the uploader.
     *
     * @throws Exception
     */
    public void upload() throws Exception;

    /**
     * Sets the content to be uploaded. Expects a String so that CLI input can be passed straight
     * to the uploader.
     *
     * @param filePath
     */
    public void setUploadContent(String filePath);

    /**
     * Returns the file to be/being uploaded. This will be a file or folder.
     *
     * @return
     */
    public File getUploadContent();

    /**
     * Sets the properties/context for this uploader to run under. The context includes details
     * such as the S3 bucket, prefix, credentials, and encryption parameters to use.
     *
     * @param properties The properties object to source context from.
     */
    public void setUploadContext(AwsProperties properties);
}

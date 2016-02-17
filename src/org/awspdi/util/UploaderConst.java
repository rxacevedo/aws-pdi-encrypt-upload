package org.awspdi.util;

/**
 * Created by vagrant on 2/16/16.
 */
public enum UploaderConst {

    ENCRYPTED(UploaderConst.DEFAULT),
    UNENCRYPTED(UploaderConst.DEFAULT);

    /**
     *  Default multipart chunk size.
     */
    private static final long DEFAULT = 5242880;

    /**
     * Chunk size for this enum.
     */
    private long chunkSize;

    UploaderConst(long chunkSize) {
        this.chunkSize = chunkSize;
    }

    public long getChunkSize() {
        return chunkSize;
    }

}

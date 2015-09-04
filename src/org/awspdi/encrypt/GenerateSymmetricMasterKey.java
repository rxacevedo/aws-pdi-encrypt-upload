package org.awspdi.encrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.awspdi.AwsProperties;
import org.junit.Assert;

import org.apache.commons.codec.binary.Base64;

/**
 * 
 * @author Kristofer Ranström
 *
 */
public class GenerateSymmetricMasterKey {

	private static String keyDir; 
    private static String keyName;
    private static String symmetricMasterKey;
    
    /**
     * 
     * @param keyDirectory to where key is saved
     * @param keyFileName name of key file
     * @param algorithm used for generator
     * @param algorithmKeyLength for initializing
     */
    public GenerateSymmetricMasterKey(final String keyDirectory,
    		final String keyFileName, final String algorithm, 
    		final int algorithmKeyLength) {
    	this.setKeyDir(keyDir);
    	this.setKeyName(keyFileName);
    	
		KeyGenerator keyGen;
		try {
			keyGen = KeyGenerator.getInstance(algorithm);
			keyGen.init(algorithmKeyLength);
			
		    SecretKey symKey = keyGen.generateKey();
		    symmetricMasterKey =  new String(Base64.encodeBase64(
		    		symKey.getEncoded()));
		    
	        System.out.println("Symmetric key saved  (base 64): "
	        		+ symmetricMasterKey);
	        
	        //Save key.
//	        saveSymmetricKey(keyDir, symKey);
	        
	        //Load key.
//	        SecretKey symKeyLoaded = loadSymmetricAESKey(awsProperties.getAlgorithm());
//	        Assert.assertTrue(Arrays.equals(symKey.getEncoded(), symKeyLoaded.getEncoded()));
	        
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }

    /**
     * 
     * @param path path for saving
     * @param secretKey name
     * @throws IOException e
     */
    public static void saveSymmetricKey(final String path, 
    		final SecretKey secretKey) 
        throws IOException {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
                secretKey.getEncoded());
        
        FileOutputStream keyfos = new FileOutputStream(path + "/" + keyName);
        keyfos.write(x509EncodedKeySpec.getEncoded());
        keyfos.close();
    }
    
    /**
     * 
     * @param algorithm ag
     * @return the secret key
     */
    public final SecretKey loadSymmetricAESKey(final String algorithm) {
        //Read private key from file.
        File keyFile = new File(keyDir + "/" + keyName);
        byte[] encodedPrivateKey = new byte[(int) keyFile.length()];
        try {
            FileInputStream keyfis = new FileInputStream(keyFile);
			keyfis.read(encodedPrivateKey);
	        keyfis.close(); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        //Generate secret key.
        return new SecretKeySpec(encodedPrivateKey, algorithm);
    }

    public String getKeyDir() {
		return keyDir;
	}

	public void setKeyDir(String keyDir) {
		GenerateSymmetricMasterKey.keyDir = keyDir;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		GenerateSymmetricMasterKey.keyName = keyName;
	}

	public String getSymmetricMasterKey() {
		return symmetricMasterKey;
	}
}
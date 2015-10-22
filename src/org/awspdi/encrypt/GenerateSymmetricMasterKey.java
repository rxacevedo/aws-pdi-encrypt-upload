package org.awspdi.encrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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
    private static SecretKey symKey;
    
    /**
     * 
     * Not every call comes with a key. If so you'll
     * be happy that this class handles master symmetric
     * key generation for you.
     * 
     * @param keyDirectory to where key is saved
     * @param keyFileName name of key file
     * @param algorithm used for generator
     * @param algorithmKeyLength for initializing
     */
    public GenerateSymmetricMasterKey(final String keyDirectory,
    		final String keyFileName, final String algorithm, 
    		final int algorithmKeyLength) {
    	this.setKeyDir(keyDirectory);
    	
		KeyGenerator keyGen;
		try {
			keyGen = KeyGenerator.getInstance(algorithm);
			keyGen.init(algorithmKeyLength);
			
		    symKey = keyGen.generateKey();
		    symmetricMasterKey = new String(Base64.encodeBase64(
		    		symKey.getEncoded()));
		    
		    // Testing purposes only
//	        System.out.println("Symmetric key saved  (base 64): "
//	        		+ symmetricMasterKey);
	        
	        //Load key.
//	        SecretKey symKeyLoaded = loadSymmetricAESKey(awsProperties
//	        .getAlgorithm());
//	        Assert.assertTrue(Arrays.equals(symKey.getEncoded(), 
//	        symKeyLoaded.getEncoded()));
	        
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
    }

    /**
     * Will save specified keyFileName into key directory
     * set by properties.
     * 
     * @param keyFileName Name of keyfile
     * @param saveEncoded true to save file encoded
     * @throws IOException e
     */
    public final void saveSingleSymmetricKey(
    		final String keyFileName, final boolean saveEncoded) {
    	
    	this.setKeyName(keyFileName);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
        		symKey.getEncoded());
        
        FileOutputStream keyfos;
        try {
			keyfos = new FileOutputStream(keyDir + "/" + keyName);
			if (saveEncoded) {
				keyfos.write(x509EncodedKeySpec.getEncoded());
			} else {
				keyfos.write(symmetricMasterKey.getBytes());				
			}
			keyfos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * 
     * @param algorithm the algorithm that was used.
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

    /**
     * 
     * @return key directory
     */
    public final String getKeyDir() {
		return keyDir;
	}

    /**
     * 
     * @param keyDirVar string to set as key directory
     */
	public final void setKeyDir(final String keyDirVar) {
		GenerateSymmetricMasterKey.keyDir = keyDirVar;
	}

	/**
	 * 
	 * @return key name
	 */
	public final String getKeyName() {
		return keyName;
	}

	/**
	 * 
	 * @param keyNameVar string to set as key name
	 */
	public final void setKeyName(final String keyNameVar) {
		GenerateSymmetricMasterKey.keyName = keyNameVar;
	}

	/**
	 * 
	 * @return symmetric master key
	 */
	public final String getSymmetricMasterKey() {
		return symmetricMasterKey;
	}
}
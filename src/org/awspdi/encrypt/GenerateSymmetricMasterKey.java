package org.awspdi.encrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.awspdi.AwsProperties;
import org.junit.Assert;

public class GenerateSymmetricMasterKey {

    private static String keyDir; 
    private static String keyName;
    private final String keyFileEnding = ".key";
    
    public GenerateSymmetricMasterKey(AwsProperties awsProperties, String keyName)  {
    	this.setKeyDir(keyDir);
    	this.setKeyName(keyName);
        
        //Generate symmetric 256 bit AES key.
        KeyGenerator symKeyGenerator;
		try {
			symKeyGenerator = KeyGenerator.getInstance(awsProperties.getAlgorithm());
	        symKeyGenerator.init(awsProperties.getAlgorithmKeyLength());
	        
	        SecretKey symKey = symKeyGenerator.generateKey();
	        
	        //Save key.
	        saveSymmetricKey(keyDir, symKey);
	        
	        //Load key.
	        SecretKey symKeyLoaded = loadSymmetricAESKey(awsProperties.getAlgorithm());
	        Assert.assertTrue(Arrays.equals(symKey.getEncoded(), symKeyLoaded.getEncoded()));
	        
	        printKey(symKeyLoaded);
	        
		} catch (NoSuchAlgorithmException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public static void saveSymmetricKey(String path, SecretKey secretKey) 
        throws IOException {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
                secretKey.getEncoded());
        
        FileOutputStream keyfos = new FileOutputStream(path + "/" + keyName);
        keyfos.write(x509EncodedKeySpec.getEncoded());
        keyfos.close();
    }
    
    public SecretKey loadSymmetricAESKey (String algorithm) {
        //Read private key from file.
        File keyFile = new File(keyDir + "/" + keyName);
        byte[] encodedPrivateKey = new byte[(int)keyFile.length()];
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
		GenerateSymmetricMasterKey.keyName = keyName + keyFileEnding;
	}
	
	public void printKey(SecretKey secretKey) {
		String decodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
		System.out.println(decodedKey);
	}
	
	public static String friendlyKey(SecretKey secretKey) {
		String decodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
		return decodedKey;
	}
	
	public static String friendlyIV(SecretKey secretKey) {
		String decodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
		return decodedKey;
	}
}
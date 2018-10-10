package es.keensoft.ciphering.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CipheringHandler {
    
    String secretKeyFactory;
    String secretKeySpec;
    String cipherInstance;
    
    public String getSecretKeyFactory() {
        return secretKeyFactory;
    }

    public void setSecretKeyFactory(String secretKeyFactory) {
        this.secretKeyFactory = secretKeyFactory;
    }

    public String getSecretKeySpec() {
        return secretKeySpec;
    }

    public void setSecretKeySpec(String secretKeySpec) {
        this.secretKeySpec = secretKeySpec;
    }

    public String getCipherInstance() {
        return cipherInstance;
    }

    public void setCipherInstance(String cipherInstance) {
        this.cipherInstance = cipherInstance;
    }
    
    public CipherBean getCipher(String passphrase) throws Exception {
        
        byte[] salt = new byte[8];
        SecureRandom sRandom = new SecureRandom();
        sRandom.nextBytes(salt);
        
        byte[] iv = new byte[128/8];
        sRandom.nextBytes(iv);
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        
        Cipher ci = Cipher.getInstance(cipherInstance);
        ci.init(Cipher.ENCRYPT_MODE, getSecretKeySpec(passphrase, salt), ivspec);
        
        CipherBean cb = new CipherBean();
        cb.setCipher(ci);
        cb.setSalt(salt);
        cb.setIv(iv);
        
        return cb;
        
    }
    
    public Cipher getDecipher(String passphrase, byte[] salt, byte[] iv) throws Exception {
        
        Cipher ci = Cipher.getInstance(cipherInstance);
        ci.init(Cipher.DECRYPT_MODE, getSecretKeySpec(passphrase, salt), new IvParameterSpec(iv));
        
        return ci;
        
    }
    
    private SecretKeySpec getSecretKeySpec(String passphrase, byte[] salt) throws Exception {
        
        SecretKeyFactory factory = SecretKeyFactory.getInstance(secretKeyFactory);
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, 10000, 128);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec skey = new SecretKeySpec(tmp.getEncoded(), secretKeySpec);
        return skey;
        
    }
    

    public static void processFile(Cipher ci, InputStream in, OutputStream out) throws Exception {
        byte[] ibuf = new byte[1024];
        int len;
        while ((len = in.read(ibuf)) != -1) {
            byte[] obuf = ci.update(ibuf, 0, len);
            if ( obuf != null ) out.write(obuf);
        }
        byte[] obuf = ci.doFinal();
        if ( obuf != null ) out.write(obuf);
    }    

}

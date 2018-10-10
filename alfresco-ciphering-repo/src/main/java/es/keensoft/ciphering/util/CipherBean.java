package es.keensoft.ciphering.util;

import javax.crypto.Cipher;

public class CipherBean {
    
    private Cipher cipher;
    private byte[] salt;
    private byte[] iv;
    
    public Cipher getCipher() {
        return cipher;
    }
    public void setCipher(Cipher cipher) {
        this.cipher = cipher;
    }
    public byte[] getSalt() {
        return salt;
    }
    public void setSalt(byte[] salt) {
        this.salt = salt;
    }
    public byte[] getIv() {
        return iv;
    }
    public void setIv(byte[] iv) {
        this.iv = iv;
    }

}

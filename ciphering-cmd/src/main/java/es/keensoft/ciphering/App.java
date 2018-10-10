package es.keensoft.ciphering;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SimpleCommandLinePropertySource;

@SpringBootApplication
public class App implements CommandLineRunner {
 
    private static Logger log = LoggerFactory.getLogger(App.class);
    
    @Value("${cipher.secret.key.factory}")
    private String secretKeyFactory;
 
    @Value("${cipher.secret.key.spec}")
    private String secretKeySpec;
    
    @Value("${cipher.instance}")
    private String cipherInstance;
    
    private String inputFile;
    private String outputFile;
    
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
    
    public void run(String... args) throws Exception {
        
        PropertySource<?> ps = new SimpleCommandLinePropertySource(args);
        if (ps != null && ps.containsProperty("input") && ps.containsProperty("output")) {
            inputFile = ps.getProperty("input").toString();
            outputFile = ps.getProperty("output").toString();
            if (ps.getProperty("secret.key.factory") != null) {
                secretKeyFactory = ps.getProperty("secret.key.factory").toString();
            }
            if (ps.getProperty("secret.key.spec") != null) {
                secretKeySpec = ps.getProperty("secret.key.spec").toString();
            }
            if (ps.getProperty("cipher.instance") != null) {
                cipherInstance = ps.getProperty("cipher.instance").toString();
            }
        } else {
            log.error("USAGE: java -jar ciphering-cmd-1.0.0.jar"
                    + " --input=file.pkcs5 --out=file.ext "
                    +  "["
                    + " --secret.key.factory=PBKDF2WithHmacSHA256 "
                    + " --secret.key.spec=AES "
                    + " --cipher.instance=AES/CBC/PKCS5Padding "
                    + "]");
            System.exit(1);
        }
        
        Console console = System.console();
        if (console == null) {
            System.out.println("No console: non-interactive mode!");
            System.exit(0);
        }
         
        System.out.print("Enter your the password: ");
        String passphrase = new String(console.readPassword());
        
        try {
        
            FileInputStream in = new FileInputStream(new File(inputFile));
            byte[] salt = new byte[8], iv = new byte[128/8];
            in.read(salt);
            in.read(iv);
            
            SecretKeyFactory factory = SecretKeyFactory.getInstance(secretKeyFactory);
            KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, 10000, 128);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec skey = new SecretKeySpec(tmp.getEncoded(), secretKeySpec);
            
            Cipher ci = Cipher.getInstance(cipherInstance);
            ci.init(Cipher.DECRYPT_MODE, skey, new IvParameterSpec(iv));
    
            File fileOut = new File(outputFile);
            try (FileOutputStream out = new FileOutputStream(fileOut)){
                processFile(ci, in, out);
            }
            
            System.out.println("Deciphered file available at " + outputFile);

        } catch (Exception e) {
            
            System.out.println("ERROR: File " + inputFile + " cannot be deciphered.");
            throw e;
            
        }

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

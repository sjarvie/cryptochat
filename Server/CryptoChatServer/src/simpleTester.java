import java.security.KeyPair;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class simpleTester {

    public static void main(String[] args) throws Exception {
    	

    	//------------------------------  AES -------------------------------
    	SecretKey sk = CryptoAlg.genAESKey();
    	String m = "This is a test for an arbitrary message which I just made up";
        AESPacket Enc = CryptoAlg.encryptAES(m,sk);
        String Dec = CryptoAlg.decryptAES(Enc,sk);

        System.out.println("Plain Text : " + m);
        System.out.println("Key : " + new String(sk.getEncoded()));
        System.out.println("Encrypted : " + Enc.data);
        System.out.println("Decrypted : " + Dec);
        
        
        
        
        // ---------------------------  RSA ----------------------------------
    	m = "This is second a test for an arbitrary message which I just made up";
        System.out.println("Plain Text : " + m);

        KeyPair kp = CryptoAlg.genRSA();
    	byte[] encM = CryptoAlg.encryptRSA(m, kp.getPublic());
    	String decM = CryptoAlg.decryptRSA(encM, kp.getPrivate());
    	
        System.out.println("Encrypted : " + new String(encM));
        System.out.println("Decrypted : " + decM);

    	
        
        
        System.out.println("Now we are going to encrypt an AES key with RSA");
        byte[] encSK = CryptoAlg.encryptRSA(new String(sk.getEncoded()), kp.getPublic());
        String decSK = CryptoAlg.decryptRSA(encSK, kp.getPrivate());
        SecretKey newSk = new SecretKeySpec(decSK.getBytes(), "AES");
        
        System.out.println("Old Key : " + new String(sk.getEncoded()));
        System.out.println("New Key : " + new String(newSk.getEncoded()));
        System.out.println("They equal??? " + newSk.equals(sk));

        
        
        
    	//test encrypting objects
        System.out.println("Now we are going to encrypt an AESFRAME key with RSA");
        SecretKeySpec skey= new SecretKeySpec(sk.getEncoded(), "AES");
        byte[] ivector = null;
    	Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
    	c.init(Cipher.ENCRYPT_MODE, skey);
    	
    	ivector = c.getIV();
        AESPacket aesp = new AESPacket("this is a test", ivector);
        byte[] encAESP = CryptoAlg.encryptRSA(CryptoAlg.encodeObject(aesp), kp.getPublic());

        AESPacket daesp = 
        		(AESPacket)CryptoAlg.decodeObject(CryptoAlg.decryptRSA(encAESP, kp.getPrivate()));
        System.out.println("They equal??? " + daesp.data.equals("this is a test"));
        
        
        
    }
}


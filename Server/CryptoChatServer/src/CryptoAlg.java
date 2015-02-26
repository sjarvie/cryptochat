import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 *
 * Cryptographic function library.
 *
 */
public class CryptoAlg {

  /**
   * Generate an 4096 bit RSA key pair.
   * @return a key pair
   */
  public static KeyPair genRSA() {
		
    KeyPairGenerator kpg = null;
    try {
      kpg = KeyPairGenerator.getInstance("RSA");
    } catch (NoSuchAlgorithmException e) {}
    
	  kpg.initialize(4096, new SecureRandom());
	  KeyPair kp = kpg.genKeyPair();
		
	  return kp;		
  }
		
  /**
   * Encrypt a message using RSA
   * @param m the message to encrypt
   * @param pub the key used in the encryption
   * @return the encrypted message contents
   */
  public static byte[] encryptRSA(String m, PublicKey pub) {
			
	  byte[] cipherData = null;
		Cipher c;
		
		try {
		  
			c = Cipher.getInstance("RSA");
			c.init(Cipher.ENCRYPT_MODE,pub);
			cipherData = c.doFinal(m.getBytes());			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cipherData;
	}
	
  /**
   * Decrypt a message using RSA
   * @param data the message contents to decrypt
   * @param priv the key used in the decryption
   * @return the unencrypted message contents
   */
  public static String decryptRSA(byte[] data, PrivateKey priv) {
		
    String s = "";
	  Cipher c;
	  try {
	    c = Cipher.getInstance("RSA");
	    c.init(Cipher.DECRYPT_MODE, priv);
			  
	    byte[] decrypted = c.doFinal(data);
	    s = new String(decrypted);

	  } catch (Exception e) {
			e.printStackTrace();
	  }
	
	  return s;
  }

	/**
   * Generates a new 128 bit AES key
   * @return the key
   */
  public static SecretKey genAESKey() {

    KeyGenerator kg = null;
    
    try {
      kg = KeyGenerator.getInstance("AES");
    } 
    catch (NoSuchAlgorithmException e1) {}
    	 
    try {
      kg.init(128, new SecureRandom());
    } 
    catch (InvalidParameterException e) {
      System.err.println("Invalid AES keygen parameter.");
      System.exit(1);
    }
    	 
    return kg.generateKey();
  }
    
  /**
   * Encrypt a message using AES
   * 
   * @param m the message to encrypt
   * @param sk the key used to encrypt
   * @param return the encoded contents of encrypted data 
   */
  public static AESPacket encryptAES(String m, SecretKey sk) {
              
    //Create a cipher and initialization vector.
    SecretKeySpec skey= new SecretKeySpec(sk.getEncoded(), "AES");
    byte[] ivector = null;
    Cipher c;
    try {
      c = Cipher.getInstance("AES/CBC/PKCS5Padding");
      c.init(Cipher.ENCRYPT_MODE, skey);
      ivector = c.getIV();

      
      //Encrypt
      byte[] ciphertext = c.doFinal(m.getBytes());
      
      //Encode
      String encValue = encodeValue(ciphertext);
      return new AESPacket(encValue,ivector);

    } catch (Exception e) {
      e.printStackTrace();  
    } 
      return null;
  }
    
  /**
   * Decrypt a message using AES
   * 
   * @param  enc the data packet to decrypt
   * @param  sk the key used in the decryption
   * @return the unencrypted message contents
   */
  public static String decryptAES(AESPacket enc, SecretKey sk) throws 
      NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, 
      InvalidAlgorithmParameterException, IOException, IllegalBlockSizeException, 
      BadPaddingException {

    //Build cipher using key and initialization vector
    Key skey = new SecretKeySpec(sk.getEncoded(), "AES");

    Cipher c = Cipher.getInstance("AES/CBC/PKCS5PADDING");
    c.init(Cipher.DECRYPT_MODE, skey, new IvParameterSpec(enc.iv));
      
    //Decode
    byte[] decodedValue = decodeValue(enc.data);
      
    //Decrypt
    byte[] decValue = c.doFinal(decodedValue);
      
    return new String(decValue);
  }
  
  
  /**
   * Encode a message using BASE64
   * 
   * @param data the data packet to encode
   * @return encoded string representation of data
   */
  public static String encodeValue(byte[] data){
    return new BASE64Encoder().encodeBuffer(data);	  		
	}
	
	/**
   * Decode a string using BASE64
   * 
   * @param data the encoded string
   * @return decoded string contents
   */
	public static byte[] decodeValue(String s) throws IOException{
	  return new BASE64Decoder().decodeBuffer(s);
	}
	
	/**
   * Decode a serialized object using BASE64
   * s the encoded string
   * @return decoded object
   */
  public static Object decodeObject(String s) throws IOException , ClassNotFoundException {
      
    byte [] data = new BASE64Decoder().decodeBuffer(s);
 
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
    Object o  = ois.readObject();
    ois.close();
    return o;
  }

  /**
   * Encode an object using BASE64
   * 
   * @param o the object
   * @return encoded string representation
   */
  public static String encodeObject( Serializable o ) {
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos;
    
    try {
      oos = new ObjectOutputStream( baos );
      oos.writeObject( o );
      oos.close();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
      
    return new String(new BASE64Encoder().encode(baos.toByteArray()));
  }
}

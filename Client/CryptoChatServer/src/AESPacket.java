import java.io.*;


/**
 * A data packet for holding AES ciphertext using CBC
 */
public class AESPacket implements Serializable {
     
  private static final long serialVersionUID = 1L;
  
  /** encrypted packet */
  String data; 
  
  /** initialization vector */
  byte[] iv;  

  /**
   * Construct a packet
   * data the encoded cipher data
   * @ivector the initialization vector used in CBC
   * @return the packet
   */
  AESPacket(String mydata,byte[] ivector){ 
    data=mydata;
  	if (ivector == null) {
  		iv = null;
  	}
  	else {
  		iv = new byte[ivector.length];
  		for (int i = 0; i < ivector.length; i++) {
  			iv[i] = ivector[i];
  		}
  	}
  }  
}
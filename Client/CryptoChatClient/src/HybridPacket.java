

import java.io.Serializable;

/**
 * A data packet for holding AES ciphertext using CBC
 */
public class HybridPacket implements Serializable {
  
  private static final long serialVersionUID = 62631337363079491L;

  /** the username of sender */
  String uname;
  
  /** The RSA encrypted AES key */
  String rsap; 
  
  /** The AES encrypted  message */
  AESPacket aesp;
 
 
  /**
   * Construct a packet
   * @u the user
   * @rsa the rsa packet
   * @aes the aes packet
   * @return a hybrid packet
   */
  public HybridPacket(String u, String rsa, AESPacket aes) {
  	uname = u;
  	aesp = aes;
  	rsap = rsa;
  }
}

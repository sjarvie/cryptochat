import java.io.Serializable;

/**
 * Application Layer packet for message passing
 */
public class CmdPacket implements Serializable{

  private static final long serialVersionUID = 8157758706160450957L;
  
  String cmd;
  String uname;
  String pass;
  String msg;
  
  /** Nonces */
  int c_nonce;
  int s_nonce;
  
  /**
   * @param c command
   * @param u username
   * @param p password
   * @param m msg
   * @param c_nonce client nonce
   * @param s_nonce server nonce
   */
  public CmdPacket(String c, String u, String p, String m, int c_nonce, int s_nonce ){
  	cmd = c;
  	uname = u;
  	pass = p;
  	msg = m;
  	this.c_nonce = c_nonce;
  	this.s_nonce = s_nonce;
  }
}

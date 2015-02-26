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
  
  /* Set of commands exchanged between server and client */
  public enum Command {
    LOGIN,
    LOGINFAIL,
    LOGINSUCCESS,
    CREATE,
    CREATESUCCESS,
    CREATEFAILDUPLICATE,
    CREATEFAILREQS,
    TALK,
    GETMEMBERS,
    BADCOMMAND
    
  }
  
  /**
   * Decode input command
   * @param s the command's string representation
   * @return the command
   */
   public static Command getCmd(String s){
     
	  CmdPacket.Command out = null; 
     if(s == null){
    	 return out;
     }
     
     if(s.equals("LOGINSUCCESS")){
       out = CmdPacket.Command.LOGINSUCCESS;
     }    
     else if(s.equals("LOGINFAIL")){
       out = CmdPacket.Command.LOGINFAIL;
     }
     else if(s.equals("CREATESUCCESS")){
       out = CmdPacket.Command.CREATESUCCESS;
     }    
     else if(s.equals("CREATEFAILDUPLICATE")){
       out = CmdPacket.Command.CREATEFAILDUPLICATE;
     }
     else if(s.equals("CREATEFAILREQS")){
    	 return CmdPacket.Command.CREATEFAILREQS;
     }
     else if(s.equals("TALK")){
       out = CmdPacket.Command.TALK;
     }
     else if(s.equals("GETMEMBERS")){
    	 out = CmdPacket.Command.GETMEMBERS;
     }
     else{
       out = CmdPacket.Command.BADCOMMAND;
     }
     return out;   
  }
  
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

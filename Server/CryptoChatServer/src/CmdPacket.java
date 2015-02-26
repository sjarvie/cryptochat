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
   * Used for server client control flow and communication
   *
   */
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
    CREATEGROUP,
    GROUPJOINSUCCESS,
    GROUPJOIN,
    LISTGROUPS,
    BADCOMMAND
    
  }  
  
  public static Command getCmd(String s){
    Command out = null;
    if(s.equals("LOGIN")){
         out = Command.LOGIN;
     }
    else if(s.equals("LOGINFAIL")){
      out = Command.LOGINFAIL;
    }
    else if(s.equals("LOGINSUCCESS")){
      out = Command.LOGINSUCCESS;
    }
       else if(s.equals("CREATE")){
       out = Command.CREATE;
     }
       else if(s.equals("CREATESUCCESS")){
         out = Command.CREATESUCCESS;
       }    
       else if(s.equals("CREATEFAILDUPLICATE")){
         out = Command.CREATEFAILDUPLICATE;
       }
       else if(s.equals("CREATEFAILREQS")){
         return Command.CREATEFAILREQS;
       }

       else if(s.equals("TALK")){
         out = Command.TALK;
       }
     else if(s.equals("GETMEMBERS")){
       out = Command.GETMEMBERS;
     }
     else if(s.equals("CREATEGROUP")){
       out = Command.CREATEGROUP;
     }
     else if (s.equals("GROUPJOINSUCCESS")){
       out = Command.GROUPJOINSUCCESS;
     }
     else if (s.equals("GROUPJOIN")){
       out = Command.GROUPJOIN;
     }
    
     else if (s.equals("LISTGROUPS")){
       out = Command.LISTGROUPS;
     }
     else{
       out = Command.BADCOMMAND;
     }
    return out;
    
    
  }
  
  
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

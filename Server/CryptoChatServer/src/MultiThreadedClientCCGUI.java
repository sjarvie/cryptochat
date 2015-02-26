import java.io.*;
import java.net.*;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.SecretKey;


/**
 * The Client Application
 *
 */
public class MultiThreadedClientCCGUI {


  /* Network Communication Layer*/
  private String host = "localhost";
  private int port = 4444;
  private static Socket clientSocket = null;
  @SuppressWarnings("unused")
  private static boolean closed = false;
  private static ObjectOutputStream out = null;
  private static ObjectInputStream in = null;


  /* GUI */
  private CCGUI cg;

  /* Crypto/Auth Variables */
  private static PublicKey serverPublicKey;
  private static SecretKey sessionKey; //symmetric key
  private static int c_nonce; //client generated nonce
  private static int s_nonce; //the server generated nonce
  private static String username = "";

 

  /**
   * Constructor
   * @param host the server host
   * @param port the server port
   * @param gui the gui used by application
   */
  public MultiThreadedClientCCGUI(String host, int port, CCGUI gui){
    this.host = host;
    this.port = port;
    cg = gui;
	
    KeyManager km = new KeyManager();
    try {
      
      //get public key
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      byte[] pubKey = CryptoAlg.decodeValue(km.getPublicKey());
      X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pubKey);
      serverPublicKey = keyFactory.generatePublic(publicKeySpec);

      //generate AES key
      sessionKey = CryptoAlg.genAESKey();

      //generate nonce
      SecureRandom random = new SecureRandom(); 
      random.setSeed(random.generateSeed(20)); 
      c_nonce = random.nextInt(Integer.MAX_VALUE);

    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("MultiClient started!");
  }

  /**
   * Connect and setup encrypted channels
   * @return whether or not the operation succeeded
   */
  public boolean connect() {
  	try {
	      clientSocket = new Socket(host, port);
    } catch (Exception e) {
      display("Error connectiong to server:" + e);
	    return false;
	  } 
 
	  String msg = "Connection accepted " + clientSocket.getInetAddress() + ":" + clientSocket.getPort();
	  display(msg);

	  try {
	    out = new ObjectOutputStream(clientSocket.getOutputStream());
	    in = new ObjectInputStream(clientSocket.getInputStream());
	  }
	  catch (IOException e) {
	    display("Exception creating streams: " + e);
	    return false;
	  }
	  // success we inform the caller that it worked
	  return true;
	}
    
    
   /**
    * Attempts to login to server with credentials
    * 
    * @param user	Client username
    * @param pass  Client password
    * @return whether or not the login was successful
    */
  public boolean attemptLogin(String user, String pass){
  	
  	username = user;
  	
  	sendLogin(user,pass);
  	CmdPacket c = getCmdPacket();
      
  	if (c != null) {
  			if (CmdPacket.getCmd(c.cmd) == CmdPacket.Command.LOGINSUCCESS) {
  				display("Login Sucessful");
  				s_nonce = c.s_nonce;
  				new ServerListener().start();
  				return true;
  			}
  			else {
  				displayLogin("Login Rejected");
  			}
  	}
  	return false;
  }
    
    
  /**
   * Attempts to create an user account with given credentials
   * 
   * @param user	Client username
   * @param pass  Client password
   * @return whether or not the creation was successful
   */
  public void attemptCreate(String user, String pass){
    
  	sendCreate(user,pass);
  	CmdPacket c = getCmdPacket();
  	
  	if(c != null){
  	    
	    //Handle Result from server
	    switch (CmdPacket.getCmd(c.cmd)) { 
	    
	      case CREATESUCCESS:
	        displayLogin("Creation Successful. Use credentials to login ");
	        break;
  	         
	      case CREATEFAILDUPLICATE:
	        displayLogin("Creation failed. USERNAME already exists!");
	        break;
  	         
	      case CREATEFAILREQS:
	        displayLogin("Creation Failed. " +
	            "Password:" +
              " 8-24 characters," +
              " atleast" +
	            " 1 Uppercase, 1 Number");
	        break;
  	        
	      default:
	        displayLogin("Error receiving response: Invalid Response");
	    }
  	} 	 
  }
    
  private void displayLogin(String msg) {
    cg.displayLogin(msg);
  }
      
  
  

    
   
	/**
	 * Display a message in the GUI window
	 * @param msg
	 */
  private void display(String msg) {
		if (cg == null) {
			System.out.println(msg);
		}
		else {
			cg.appendChat(msg + "\n");		
		}
			
	}
	
	
  /**
   * Refresh a group listing in the GUI window
   * @param msg
   */
	private void refreshMembers(String s) {
	
	  if(cg == null){
			System.out.println(s);
		}
		else{
			cg.clearMembers();
			
			//add group members
			String[] data = s.split("|");
			for (int i = 0; i < data.length; i++){
				cg.appendMembers(data[i]+ "\n");
				
			}
		}
	}  
	/**
	 * Refresh List of joined groups
	 * @param msg
	 */
	 private void refreshGroupList(String msg) {
     // TODO Auto-generated method stub
     
   }
	
	
	
  

  
	/**
	 * Receives messages and displays them to console or GUI
	 *
	 */
  class ServerListener extends Thread {
	 
    /**
     * Run method for the ServerListener
     */
    public void run() {
      CmdPacket c;
      
			while(true) {
				c = null;
	    	c = getCmdPacket();
		    	
	    	if(c != null){
	    	  
	    	  switch(CmdPacket.getCmd(c.cmd)){
	    	  
			    		case TALK:{
			    			if(c.msg.equals("/quit")){
			    				disconnect();
			    				closed = true;
			    			}
			    			else {
			    				display(c.msg);
			    			}	
			    			break;
			    		}
			    		case GETMEMBERS: {
			    			refreshMembers(c.msg);
			    			break;
			    		}
			    		case LISTGROUPS: {
			    		  refreshGroupList(c.msg);
			    		  break;
			    		}
			    		default:{}
	    	  }		    		
	    	}
			}
    }

   
  }
  

 


  /**
   * Creates login packet and sends to server
   * 
   * @param username
   * @param password
   * @param nonce, the generated nonce for the session
   */
  private static void sendLogin(String username, String password){
    CmdPacket cmd = new CmdPacket("LOGIN",username,password, "", c_nonce,0);
    String data = CryptoAlg.encodeObject(cmd);
    AESPacket aesp = encAES(data);
    
    //Hybrid encrypt
    X509EncodedKeySpec ks = new X509EncodedKeySpec(sessionKey.getEncoded());
    byte[] encK = encRSA(new String(ks.getEncoded()), serverPublicKey);
    HybridPacket p = new HybridPacket(username, CryptoAlg.encodeValue(encK),aesp);
    
    sendToServer(p);
  }
  
  /**
   * Creates account creation packet and sends to server
   * 
   * @param username
   * @param password
   */
  private static void sendCreate(String username, String password){
    CmdPacket cmd = new CmdPacket("CREATE",username,password, "", c_nonce,0);
    String data = CryptoAlg.encodeObject(cmd);
    AESPacket aesp = encAES(data);

    //Hybrid encrypt
    X509EncodedKeySpec ks = new X509EncodedKeySpec(sessionKey.getEncoded());
    byte[] encK = encRSA(new String(ks.getEncoded()), serverPublicKey); 
    HybridPacket p = new HybridPacket(username, CryptoAlg.encodeValue(encK),aesp);
    sendToServer(p);
  }
  
	
	
	 /**
	   * Send a encrypted request for current group members 
	   */
	  void sendRequestGroupMembers() {
	    CmdPacket cmd = new CmdPacket("GETMEMBERS",username,"", "", c_nonce,s_nonce);
	    String data = CryptoAlg.encodeObject(cmd);
	    AESPacket aesp = encAES(data);

	    //Hybrid encrypt
	    X509EncodedKeySpec ks = new X509EncodedKeySpec(sessionKey.getEncoded());
	    byte[] encK = encRSA(new String(ks.getEncoded()), serverPublicKey); 
	    HybridPacket p = new HybridPacket(username, CryptoAlg.encodeValue(encK),aesp);
	    sendToServer(p);
	  }

  
  
	/**
	 * When something goes wrong
	 * Close streams/socket
	 */
	void disconnect() {
		try { 
			if(in != null) in.close();
			if(out != null) out.close();
			if(clientSocket != null) clientSocket.close();
		}
		catch(Exception e) {}
		if(cg != null){
			cg.connectionFailed();
		}
		
		
	}
  
  
  /**
   * Sends a message to the server
   * 
   * 
   * @msg the message to send
   */
  void sendTalk(String msg){
    CmdPacket cmd = new CmdPacket("TALK",username,"", msg, c_nonce,s_nonce);
    String data = CryptoAlg.encodeObject(cmd);
    AESPacket aesp = encAES(data);
    HybridPacket p = new HybridPacket(username, null,aesp); // no need to hybrid encrypt, just send {msg,r}k_cs
    sendToServer(p);
  }

  
  
  
  
  
  
  
//------------------------------------------------------------------------------------------
  //								COMM
  //------------------------------------------------------------------------------------------
  //sends packet to server
  private static void sendToServer(HybridPacket p) {
  	try {
			out.writeObject(p);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
  	
  }

  /**
     * Receive packet from client
     * @param c  the client socket
     * @return the received packet
     */
    private static HybridPacket recFromServer() throws IOException, ClassNotFoundException{
      HybridPacket p = (HybridPacket)in.readObject();
      return p;
    }

    /**
    * Receives a Hybrid packet from server and gets its command packet contents
    * @return A command packet or null 
    */
    private CmdPacket getCmdPacket(){
      //get a packet
      HybridPacket p = null;
    try {
      p = recFromServer();
    } catch (Exception e) {}
    

      CmdPacket cmd = null;
      if(p != null){
      //handle command packet
      try {
        cmd = (CmdPacket)CryptoAlg.decodeObject(decAES(p.aesp.data, p.aesp.iv));
        if(		(!p.uname.equals(cmd.uname)) || 
        		(cmd.c_nonce != c_nonce) || 
        		(!p.uname.equals(username)) ||
        		((CmdPacket.getCmd(cmd.cmd) == CmdPacket.Command.TALK) && (cmd.s_nonce != s_nonce))
        		)
        	{
        	System.out.println("packet compromised");
            return null;
        	}
         }
      catch (ClassNotFoundException e) {} 
      catch (Exception e) {}
      
     
      
  
      }   
      return cmd;
          
  }

    

         

           
 

    //------------------------------------------------------------------------------------------
  //								RSA
  //------------------------------------------------------------------------------------------

    
    
    //encrypt a message with RSA
    private static byte[] encRSA(String m, PublicKey pub){
    	return CryptoAlg.encryptRSA(m, pub);
    	
    }
    

    
    
    //------------------------------------------------------------------------------------------
    //								AES
    //------------------------------------------------------------------------------------------
    
    //encrypt a message with AES
    private static AESPacket encAES(String m){
    	return CryptoAlg.encryptAES(m, sessionKey);
    }
  
  //decrypt a message with AES
    private static String decAES(String enc, byte[] iv) throws Exception{
    	
    	AESPacket a = new AESPacket(enc, iv);
    	return CryptoAlg.decryptAES(a, sessionKey);
    }
    




}
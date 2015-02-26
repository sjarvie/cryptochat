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
public class MultiThreadedClient {

  /* Network Communication Layer*/
  private String host = "localhost";
  private int port = 4444;
  private static Socket clientSocket = null;
  private static boolean closed = false;
  private static ObjectOutputStream out = null;
  private static ObjectInputStream in = null;

  /* GUI */
  private ClientGUI cg;

  /* Crypto/Auth Variables */
  private static PublicKey serverPublicKey;
  private static SecretKey sessionKey; //symmetric key
  private static int c_nonce; //client generated nonce
  private static int s_nonce; //the server generated nonce
  private static String username = "";

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
   * Constructor
   * @param host the server host
   * @param port the server port
   * @param gui the gui used by application
   * 
   */
  public MultiThreadedClient(String host, int port, ClientGUI gui){
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
			if (getCmd(c.cmd) == Command.LOGINSUCCESS) {
				display("Login Sucessful");
				s_nonce = c.s_nonce;
				new ServerListener().start();
				return true;
			}
			else {
				display("Login Rejected");
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
	    switch (getCmd(c.cmd)) { 
	    
	      case CREATESUCCESS:
	        display("Creation Successful. Use credentials to login ");
	        break;
  	         
	      case CREATEFAILDUPLICATE:
	        display("Creation failed. USERNAME already exists!");
	        break;
  	         
	      case CREATEFAILREQS:
	        display("Creation Failed. " +
	            "Password:" +
              " 8-24 characters," +
              " atleast" +
	            " 1 Uppercase, 1 Number");
	        break;
  	        
	      default:
	        display("Error receiving response: Invalid Response");
	    }
  	} 	 
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
	private void refreshGroup(String s) {
	
	  if(cg == null) {
			System.out.println(s);
		}
		else {
			cg.clearGroup();
			cg.appendChat(s + "\n");	
			
			//add group members
			String[] data = s.split("|");
			for (int i = 0; i < data.length; i++){
				cg.appendGroup(data[i]+ "\n");
				
			}
		}
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
	    	  
	    	  switch(getCmd(c.cmd)){
	    	  
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
			    			refreshGroup(c.msg);
			    			break;
			    		}
			    		default:{}
	    	  }		    		
	    	}
			}
    }
  }
  
  /**
   * Decode input command
   * @param s the command's string representation
   * @return the command
   */
   private static Command getCmd(String s){
     
     Command out = null; 
     if(s == null){
    	 return out;
     }
     
     if(s.equals("LOGINSUCCESS")){
       out = Command.LOGINSUCCESS;
     }    
     else if(s.equals("LOGINFAIL")){
       out = Command.LOGINFAIL;
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
     else{
       out = Command.BADCOMMAND;
     }
     return out;   
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
	 * Close a stream and socket
	 */
	void disconnect() {
		try { 
			if(in != null) { in.close(); }
			if(out != null){ out.close(); }
			if(clientSocket != null) { clientSocket.close(); }
		}
		catch(Exception e) {
		  System.out.println("Unable to close connection");
		}
		if(cg != null) { cg.connectionFailed(); }
	}
  
  /**
   * Sends a message to the server
   * @param msg the message to send
   */
  void sendTalk(String msg){
    CmdPacket cmd = new CmdPacket("TALK",username,"", msg, c_nonce,s_nonce);
    String data = CryptoAlg.encodeObject(cmd);
    AESPacket aesp = encAES(data);
    HybridPacket p = new HybridPacket(username, null,aesp); // no need to hybrid encrypt, just send {msg,r}k_cs
    sendToServer(p);
  }

  /**
   * Sends a hybrid packet to the server
   * 
   * @param p the packet to send
   */
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
  private static HybridPacket recFromServer() throws IOException, ClassNotFoundException {
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
        		((getCmd(cmd.cmd) == Command.TALK) && (cmd.s_nonce != s_nonce))
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

  /**
   * Crypto helper method for RSA encryption
   * @param m the string to encrypt
   * @param pub the public key used for encryption
   * @return the encrypted bytes
   */
  private static byte[] encRSA(String m, PublicKey pub){
  	return CryptoAlg.encryptRSA(m, pub);  	
  }
    
  /**
   * Crypto helper method for AES encryption 
   * @param m the string to encrypt
   * @return the AES data packet
   */
  private static AESPacket encAES(String m){
  	return CryptoAlg.encryptAES(m, sessionKey);
  }
  
  /**
   * Crypto Helper for AES decryption
   * @param enc the encrypted message
   * @param iv the initialization vector used in CBC
   * @return the decrypted string
   * @throws Exception
   */
  private static String decAES(String enc, byte[] iv) throws Exception{    	
  	AESPacket a = new AESPacket(enc, iv);
  	return CryptoAlg.decryptAES(a, sessionKey);
  }
}
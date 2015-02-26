import java.io.*;
import java.net.*;
import javax.crypto.*;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import javax.crypto.spec.*;

/**
 * A chat server that delivers public and private messages.
 */
public class MultiThreadedServer {

  /** Network Interface variables */ 
  private static int PORT;
  private static ServerSocket serverSocket = null;
  private static Socket clientSocket = null;

  /** This chat server can accept up to maxClientsCount clients' connections.*/
  private static final int maxClientsCount = 10;
  private static final clientThread[] threads = new clientThread[maxClientsCount];

  /** Crypto Vars */
  private PublicKey publicKey;
  private PrivateKey privateKey;

  /** Database */
  private KeyManager keyManager;
  private UserManager userManager;

  /** Application Layer */
  public enum Command {
    LOGIN,
    LOGINFAIL,
    LOGINSUCCESS,
    CREATE,
    CREATESUCCESS,
    CREATEFAIL,
    GETMEMBERS,
    TALK,
    BADCOMMAND
    
  }

  /**
   * Initializes Server and builds new RSA Key
   */
  public void initServer() {
  
    // create socket
    try {
      serverSocket = new ServerSocket(PORT);
    } 
    catch (IOException e) {
      System.out.println("Could not listen on port: "+ PORT + ".");
      System.exit(-1);
    }
      
    // Get Keys 
    keyManager = new KeyManager();
    KeyFactory keyFactory;
    try {
      
      // Public Key
      keyFactory = KeyFactory.getInstance("RSA");
      byte [] pubKey = CryptoAlg.decodeValue(keyManager.getPublicKey());
      X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pubKey);
      publicKey = keyFactory.generatePublic(publicKeySpec);
    
      // Private Key
      byte[] privKey = CryptoAlg.decodeValue(keyManager.getPrivateKey());
      PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privKey);
      privateKey = keyFactory.generatePrivate(privateKeySpec);
            
    } 
    catch (NoSuchAlgorithmException e) {e.printStackTrace();} 
    catch (IOException e) {e.printStackTrace();} 
    catch (InvalidKeySpecException e) {e.printStackTrace();}
    
        
    
    // Init User Manager and Table
    userManager = new UserManager();
    userManager.initUserTable();    
    System.out.println("MultiThreaded Server bound to IP: "+ serverSocket.getInetAddress().getHostAddress());
    System.out.println("MultiThreaded Server Started on port " + PORT + "."); 
        
  }

  /**
   * Start the server and handle clients
   * @throws Exception
   */
  public void startServer() throws Exception{
    
    // Start Server
    initServer();

    
    // Handle Clients
    while (true) {
      try {
        // Grab a client socket
        clientSocket = serverSocket.accept();
        System.out.println("Accepted a client socket");
        
        //Find thread to handle client
        int i;
        for (i = 0; i < maxClientsCount; i++) {
          if (threads[i] == null) {
            (threads[i] = new clientThread(clientSocket, threads, publicKey, privateKey, userManager)).start();
            break;
          }
        }
        // Reject if too busy
        if (i == maxClientsCount) {
          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }
    
  /**
   * Start server
   * @param p the port to run server
   */
  public MultiThreadedServer(int p){
	  PORT = p;
  }

  /**
   * starts a server on port 4444
   */
  public static void main(String args[]) {

    // The default port number.

    MultiThreadedServer s = new MultiThreadedServer(4444);
    try {
      s.startServer();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }



  /**
   * Generate RSA Keys for server use
   */
  @SuppressWarnings("unused")
  private void buildRSA(){
        KeyPair kp;
        
      try {
       kp = CryptoAlg.genRSA();

       publicKey = kp.getPublic();
        System.out.println("Generated Public Key: " + publicKey.toString());

        privateKey = kp.getPrivate();
        System.out.println("Generated Private Key: " + privateKey);

    } catch (Exception e) {
        System.out.println("Unable to generate RSA.");
        System.exit(-1);
    }
  }
   
}

/**
 * The chat client thread. This client thread opens the input and the output
 * streams for a particular client, ask the client's name, informs all the
 * clients connected to the server about the fact that a new client has joined
 * the chat room, and as long as it receive data, echos that data back to all
 * other clients. The thread broadcasts the incoming messages to all clients and
 * routes the private message to the particular client. When a client leaves the
 * chat room this thread informs also all the clients about that and terminates.
 */
class clientThread extends Thread {


  /** Network Communication Layer */
  private Socket clientSocket = null;
  private ObjectInputStream in  = null;
  private ObjectOutputStream out = null;


  /** Thread Variables */
  private final clientThread[] threads;
  private int maxClientsCount;
  private boolean isLoggedIn = false;



  /** Crypto Variables */
  @SuppressWarnings("unused")
  private static PublicKey serverPublicKey;
  private static PrivateKey serverPrivateKey;
  private SecretKey sessionKey;     //AES
  private int c_nonce;          //client -> server nonce
  private int s_nonce;          //server -> client nonce
  private String username;
  
  /** Application Layer */
  private UserManager userManager;

  /**
   * Constructor for a client thread
   * @param c the client socket
   * @param threads the shared array of client threads
   * @param pub the server public key
   * @param priv the server private key
   * @param um the user manager
   */
  public clientThread(Socket c, clientThread[] threads, PublicKey pub, PrivateKey priv, UserManager um) {
    clientSocket = c;
    serverPublicKey = pub;
    serverPrivateKey = priv;
    this.threads = threads;
    maxClientsCount = threads.length;
    userManager = um;
    
    // generate a server nonce specific to this client
    SecureRandom random_s = new SecureRandom(); 
    random_s.setSeed(random_s.generateSeed(20)); 
    s_nonce = random_s.nextInt(Integer.MAX_VALUE);
  }
  

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
    BADCOMMAND
  }  
  

  
  private Command getCmd(String s){
    Command out = null;
    if(s.equals("LOGIN")){
         out = Command.LOGIN;
     }    
       else if(s.equals("CREATE")){
       out = Command.CREATE;
     }
     else if(s.equals("GETMEMBERS")){
    	 out = Command.GETMEMBERS;
     }
     else if(s.equals("CREATEGROUP")){
    	 out = Command.CREATEGROUP;
     }
     else if(s.equals("TALK")){
       out = Command.TALK;
     } 
    return out;
  }
  
  /**
   * Send a packet to client
   * @param p the packet to send
   * @throws IOException 
   */
  private void sendToClient(HybridPacket p) throws IOException{
    out.writeObject(p);
    out.flush();
  }
  
  /**
   * Send a TALK message to client
   * @param msg the message
   */
  private void sendMsgToClient(String msg) throws IOException{
    CmdPacket cmd = new CmdPacket("TALK",username,"", msg, c_nonce, s_nonce);
    String data = CryptoAlg.encodeObject(cmd);
    AESPacket aesp = encAES(data, sessionKey);
  
    HybridPacket p = new HybridPacket(username, null,aesp); // no need to hybrid encrypt, just send {msg,r}k_cs
    sendToClient(p);
  }
    
  /**
   * Sends group member list message to client
   * @param msg the message
   */
  private void sendMembersToClient(String msg) throws IOException{
    CmdPacket cmd = new CmdPacket("GETMEMBERS",username,"", msg, c_nonce, s_nonce);
    String data = CryptoAlg.encodeObject(cmd);
    AESPacket aesp = encAES(data, sessionKey);
    
    HybridPacket p = new HybridPacket(username, null,aesp); // no need to hybrid encrypt, just send {msg,r}k_cs
    sendToClient(p);
  }
    
  /**
   * Receive packet from client
   * @param c  the client socket
   * @return the received packet
 * @throws IOException 
   */
  private HybridPacket recFromClient() throws ClassNotFoundException, IOException{
    HybridPacket p = (HybridPacket)in.readObject();
    
    return p;
  }
        
  /**
   * RSA decryption helper
   * @param enc the encrypted byte []
   * @return the unencrypted string
   */
  private String decRSA(byte[] enc){
    return CryptoAlg.decryptRSA(enc, serverPrivateKey);
  }
        
  /**
   *  AES encryption helper     
   * @param m the string the encrypt
   * @param k_cs the secret key used to encrypt
   * @return encrypted packet
   */
  private AESPacket encAES(String m, SecretKey k_cs) {
  	return CryptoAlg.encryptAES(m, k_cs);
  }
  
  /**
   * AES decryption helper
   * @param enc the encrypted string
   * @param iv the byte[] for CBC
   * @param k_cs the secret key
   * @return the decrypted data
   */
  private String decAES(String enc, byte[] iv, SecretKey k_cs) {
    AESPacket a = new AESPacket(enc, iv);
  	try {
  	  return CryptoAlg.decryptAES(a, k_cs);
  	} catch (Exception e) {
  	  e.printStackTrace();
  	} 
  	return null;
  }
  
  /**
   * Builds account login success notification packet
   * @return the packet
   */
  private HybridPacket buildLoginSuccess(){
    CmdPacket cmd = new CmdPacket("LOGINSUCCESS", username, null, "", c_nonce, s_nonce);
    String data = CryptoAlg.encodeObject(cmd);
  
    AESPacket aesp = encAES(data, sessionKey);
  
    HybridPacket p = new HybridPacket(username, null,aesp);
    return p;
  } 

  /**
   * Builds account login failure notification packet
   * @return the packet
   */
  private HybridPacket buildLoginFail(){
    CmdPacket cmd = new CmdPacket("LOGINFAIL", username, null, "", c_nonce,s_nonce);
    String data = CryptoAlg.encodeObject(cmd);

    AESPacket aesp = encAES(data, sessionKey);

    HybridPacket p = new HybridPacket(username, null,aesp);
    return p;
  } 
        
   /**
   * Builds account creation success notification packet
   * @return the packet
   */
  private HybridPacket buildCreateSuccess(){
    CmdPacket cmd = new CmdPacket("CREATESUCCESS", username, null, "", c_nonce,s_nonce);
    String data = CryptoAlg.encodeObject(cmd);
  
    AESPacket aesp = encAES(data, sessionKey);
  
    HybridPacket p = new HybridPacket(username, null,aesp);
    return p;
  } 
        
  /**
   * Creates account creation failure notification packet
   * @param fail either CREATEFAILDUPLICATE or CREATEFAILREQS
   */
  private HybridPacket buildCreateFail(String fail){
    CmdPacket cmd = new CmdPacket(fail, username, null, "", c_nonce,s_nonce);
    String data = CryptoAlg.encodeObject(cmd);

    AESPacket aesp = encAES(data, sessionKey);

    HybridPacket p = new HybridPacket(username, null,aesp);
    return p;
  }

  /**
   * The run method to communicate with server
   */
  public void run() {
    int maxClientsCount = this.maxClientsCount;
    clientThread[] threads = this.threads;

    try {
      
      // Create input and output streams for this client.
      out = new ObjectOutputStream(clientSocket.getOutputStream());
      in = new ObjectInputStream(clientSocket.getInputStream());


      // Verify User and setup encrypted connection
      boolean running = true;
      while (running){
        
        // Receive and handle packets
        HybridPacket p = recFromClient();
        if(p != null){

          // Decrypt session key
          if(p.rsap != null){
            byte[] key_data = CryptoAlg.decodeValue(p.rsap);
            String key = decRSA(key_data);
            this.sessionKey = new SecretKeySpec(key.getBytes(),"AES");
          }


          // Retrieve command packets
          CmdPacket cmd = null;
          if(p.aesp != null && p.aesp.data != null && p.aesp.iv != null){
            cmd = (CmdPacket)CryptoAlg.decodeObject(decAES(p.aesp.data, p.aesp.iv, sessionKey));
            c_nonce = cmd.c_nonce;
            if(!p.uname.equals(cmd.uname)){
                 System.out.println("Packet Compromised, client refused");
            }
          }
            
          // Handle data based on command
          if(cmd != null){
          
        	  switch(getCmd(cmd.cmd)){
        
        	  	// Authenticate a creation request
        	  	case CREATE: {
        	  		System.out.println("Received a CREATE");
        	  		
                
        	  		if (userManager.createAccount(cmd.uname, cmd.pass)) {
        	  			HybridPacket outP = buildCreateSuccess();
        	  			sendToClient(outP);
        	  			System.out.println("Account " + cmd.uname+  "created");
        	  		}
        	  		else {
        	  			System.out.println("Create for " + cmd.uname+  "failed");
        	  		
        	  			//send "why" it failed
        	  			HybridPacket outP;
        	  			if (userManager.usernameExists(cmd.uname)) {
        	  				outP = buildCreateFail("CREATEFAILDUPLICATE");
        	  			}
        	  			else {
        	  				outP = buildCreateFail("CREATEFAILREQS");
        	  			}
        	  			sendToClient(outP); 
                }
        	  		break;  
        	  	}
              

        	  	//Authenticate a login
        	  	case LOGIN:{
        	  		System.out.println("Received a LOGIN");

        	  		// Check if valid login
        	  		if (userManager.LoginTest(cmd.uname, cmd.pass)) {
        	  		  
        	  			//find your thread, if able, and set as logged in
        	  		  username = cmd.uname;
        	  		  isLoggedIn = true;

        	  			// Atomic send login success
        	  			synchronized (this) {
        	  				for (int i = 0; i < maxClientsCount; i++) {
        	  					if (threads[i] != null && threads[i] == this) {
        	  						
        	  						HybridPacket outP = buildLoginSuccess();
        	  						sendToClient(outP);

        	  						System.out.println("Account " + cmd.uname + "logged in");
        	  						break;
        	  					}
        	  				}
        	  			}       
        	  		}
        	  		else{
        	  		  
        	  		  // Atomic send login failure
        	  			synchronized (this) {
        	  				for (int i = 0; i < maxClientsCount; i++) {
        	  					if (threads[i] != null && threads[i] == this) {
        	  						HybridPacket outP = buildLoginFail();
        	        	  			sendToClient(outP);  
        	  						System.out.println("Account " + cmd.uname + "gave wrong password");
        	  						break;
        	  					}
        	  				} 
        	  			}               
        	  		}
        	  		break;
        	  	}
      
        	  	//receive messages 
        	  	case TALK:{
        	  		
        	  		//sanity check and prevent replay attack
        	  		if(isLoggedIn && cmd.msg != null && (cmd.s_nonce == s_nonce)){
        	  			
        	  		  //client exit
        	  			if (cmd.msg.equals("/quit")) {
        	  				running = false;
        	  				break;
        	  			}
        	  			
        	  			// Log Message
        	  			System.out.println("<"+cmd.uname+">" + cmd.msg);

        	  			// Send message to whoever needs it 
        	  			synchronized (this) {
        	  				for (int i = 0; i < maxClientsCount; i++) {

        	  					// Pick and choose which clients to send based on group
        	  					if (threads[i] != null && threads[i].username != null) {
        	  						System.out.println("Sending to " + threads[i].username);
        	  						threads[i].sendMsgToClient("<"+cmd.uname+"> " + cmd.msg);
        	  					}
        	  				}
        	  			}
        	  		}

        	  		break;
        	  	}
        	  	
        	  	case GETMEMBERS:{
        	  	  
        	  	  //sanity check and prevent replay attack
        	  		if(isLoggedIn && (cmd.s_nonce == s_nonce)){
        	  			
        	  			ArrayList<String> g_ids =  userManager.getGroupMembership(username);
        	  			ArrayList<String> names = (new GroupManager().getMembers(g_ids.get(0)));
        	  			
        	  			StringBuilder sb = new StringBuilder();
        	  			for (String s : names) {
        	  			    sb.append(s);
        	  			    sb.append("|");
        	  			}
        	  			
        	  			String members = sb.toString();
        	  			synchronized (this) {
        	  				for (int i = 0; i < maxClientsCount; i++) {
        	  					if (threads[i] != null && threads[i] == this) {
        	  						sendMembersToClient(members);
        	  						break;
        	  					}
        	  				}
                  
        	  			}
        	  			
        	  			
        	  			
        	  		}
        	  		break;
        	  		
        	  		
        	  	}
        	  	
        	  	default:{
        	  	}
        	  }
          }       
        }
      }

      /* Clean  up thread and notify others*/
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
        	//send to group members only
        	if (threads[i] != null && threads[i] != this
        			&& threads[i].username != null) {
        		threads[i].sendMsgToClient(username + " left chat!");        
        	}
        }
      }
    
      
      
      // Free up thread for a later connection
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] == this) {
            System.out.println(threads[i].username + " has disconnected");
            threads[i] = null;
          }
        }
      }

      in.close();
      out.close();
      clientSocket.close();    
  }
    catch (IOException e) {
    } 
    catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }
}
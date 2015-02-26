import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

  

/**
 * The Client with its GUI
 */
public class ClientGUI extends JFrame implements ActionListener {

 
  private static final long serialVersionUID = 1L;

  /** To hold the username and later on the messages */
  private JTextField user;
  private JTextField pass;
  
  /** the message prompt for user to submit msgs */
  private JTextField chatLine;

  /** to hold the server address an the port number */
  private JTextField tfServer, tfPort;

  /** Buttons and Areas */
  private JButton login, logout, getMembers, createAccount, getTxt;
  private JTextArea chatroom;
  private JTextArea grouplist;
  
  /** if it is for connection */
  @SuppressWarnings("unused")
  private boolean connected;
  
  /** the Client object */
  private MultiThreadedClient client;
  
  /** the default port number */
  private int defaultPort;
  private String defaultHost;
  
  /**
   * Constructor for GUI
   * @param host the default host
   * @param port the default port
   * @return the GUI
   */
  ClientGUI(String host, int port) {
    super("CryptoChat Client");
    defaultPort = port;
    defaultHost = host;
    
    // The NorthPanel with:
    JPanel northPanel = new JPanel(new GridLayout(3,1));
    
    // Chat and Group Panel
    JPanel centerPanel = new JPanel(new BorderLayout());
    
   
    // Server/Port
    JPanel serverAndPort = new JPanel(new GridLayout(1,5, 1, 3));
    tfServer = new JTextField(host);
    tfPort = new JTextField("" + port);
    
    tfPort.setHorizontalAlignment(SwingConstants.RIGHT);
    serverAndPort.add(new JLabel("Server Address:  "));
    serverAndPort.add(tfServer);
    
    serverAndPort.add(new JLabel("Port Number:  "));
    serverAndPort.add(tfPort);
    serverAndPort.add(new JLabel(""));

    northPanel.add(serverAndPort);

    //Username
    user = new JTextField("Username");
    user.setBackground(Color.WHITE);
    northPanel.add(user);

    //Password
    pass = new JTextField("Password");
    pass.setBackground(Color.WHITE);
    northPanel.add(pass);
    
    add(northPanel, BorderLayout.NORTH); 
    
  
    // Chat Room
    chatroom = new JTextArea("Welcome to the Chat room\n", 80, 80);
    centerPanel.add(new JScrollPane(chatroom),BorderLayout.CENTER);
    chatroom.setEditable(false);
    
    //Group List
    grouplist = new JTextArea("Group Members\n");
    centerPanel.add(new JScrollPane(grouplist), BorderLayout.EAST);
    grouplist.setEditable(false);
    
    // add Chat and Group Panel
    add(centerPanel, BorderLayout.CENTER);
    
 
    // the 4 buttons
    
    //login
    login = new JButton("Login");
    login.addActionListener(this);
    
    //create
    createAccount = new JButton("Create Account");
    createAccount.addActionListener(this);
    
    //getTxt
    getTxt = new JButton("GetTxt");
    getTxt.addActionListener(this);
    getTxt.setEnabled(false);

    
    //logout
    logout = new JButton("Logout");
    logout.addActionListener(this);
    logout.setEnabled(false);       
    
    //getMembers
    getMembers = new JButton("getMembers");
    getMembers.addActionListener(this);
    getMembers.setEnabled(false);      
      
     //Chatline 
    JPanel southPanel = new JPanel(new GridLayout(2,1));
    chatLine = new JTextField();
    southPanel.add(chatLine);
    
    //Add Buttons and Panels
    JPanel buttons = new JPanel();
    buttons.add(login);
    buttons.add(logout);
    buttons.add(createAccount);
    buttons.add(getMembers);
    buttons.add(getTxt);
    southPanel.add(buttons);
    add(southPanel, BorderLayout.SOUTH);
    
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(600, 600);
    setVisible(true);
    user.requestFocus();
 
  }
 
    /**
     * Called by the Client to append text in the TextArea
     * @param str the text to add
     */
  void appendChat(String str) {
    chatroom.append(str);
    chatroom.setCaretPosition(chatroom.getText().length() - 1);
  }
    
  /**
   * Called by the Client to append text in the TextArea
   * @param str
   */
  void appendGroup(String str) {
    grouplist.append(str);
    grouplist.setCaretPosition(chatroom.getText().length() - 1);
  }
  
  /**
   * Clears the group for refreshing list
   */
  void clearGroup(){
  	grouplist.setText("");
  }
  
  
  /**
   *  notify the user that the connection has failed
   *  
   */
  void connectionFailed() {
    login.setEnabled(true);
    createAccount.setEnabled(true);
    logout.setEnabled(false);
    getMembers.setEnabled(false);
    user.setText("Username");
    pass.setText("Password");
    
    // reset port number and host name as a construction time
    tfPort.setText("" + defaultPort);
    tfServer.setText(defaultHost);
    
    // let the user change them
    tfServer.setEditable(false);
    tfPort.setEditable(false);
    
    // don't react to a <CR> after the username
    user.removeActionListener(this);
    connected = false;
  }
       
  /**
  * Button or JTextField clicked
  * @param e the event to handle
  */
  public void actionPerformed(ActionEvent e) {
    Object o = e.getSource();
    
    // if it is the Logout button
    if(o == logout) {
      client.sendTalk("/quit");
      client.disconnect();
      return;
    }
    
    // if it the who is in button
    else if(o == getMembers) {
    	client.sendRequestGroupMembers();             
      return;
    }
    
    //Create account button
    if(o == createAccount){   
          
      //Dont submit empty queries
      String username = user.getText().trim();
      String password = pass.getText().trim();
      if((username.length() == 0) || (password.length() == 0)){
          return;
      }

      String host = tfServer.getText().trim();
      if(host.length() == 0)
          return;
      String portNumber = tfPort.getText().trim();
      if(portNumber.length() == 0){
          return;
      }
      int port = 0;
      try {
          port = Integer.parseInt(portNumber);
      }
      catch(Exception en) {
          return;   
      }
      
      // try creating a new Client with GUI
      client = new MultiThreadedClient(host,port,this);       
      
      if(!client.connect()){
        return;
      }
      client.attemptCreate(username,password);
      client.disconnect();
      return;
    }
 
    // ok it is coming from the JTextField
    if(o == chatLine) {
        // just have to send the message
        client.sendTalk(chatLine.getText());
        chatLine.setText("");
        return;
    }
     
    //Login Button
    if(o == login) {
      String username = user.getText().trim();
      String password = pass.getText().trim();
      
      if((username.length() == 0) || (password.length() == 0)) {
        return;
      }
            
      String host = tfServer.getText().trim();
      if(host.length() == 0){
          return;
      }
      
      String portNumber = tfPort.getText().trim();
      if(portNumber.length() == 0){
        return;
      }
      
      int port = 0;
      try {
        port = Integer.parseInt(portNumber);
      }
      catch(Exception en) {
          return;   
      }
 
      // try creating a new Client with GUI
      client = new MultiThreadedClient(host,port,this);
        
      if(!client.connect()){
          return;
      }
      //attempt to login
      if(!client.attemptLogin(username,password)){
          client.disconnect();
          return;
      }    
      connected = true;
       
      // disable login button
      login.setEnabled(false);
      
      //clear password
      pass.setText("");
      
      // enable buttons
      logout.setEnabled(true);
      getMembers.setEnabled(true);
      getTxt.setEnabled(true);
      
      // disable the Server and Port JTextField
      tfServer.setEditable(false);
      tfPort.setEditable(false);
      
      // Action listener for when the user enter a message
      
      chatLine.setText("");
      chatLine.addActionListener(this);   
    }
  }
 
  /**
   * run the gui
   * 
   */
  public static void main(String[] args) {
    new ClientGUI("localhost", 4444);
  }
    
}

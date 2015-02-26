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
  
  /** the message prompt for user to submit msgs, join groups */
  private JTextField chatLine, joinLine;

  /** to hold the server address an the port number */
  private JTextField tfServer, tfPort, tfUser, tfPass, joinText;

  /** Panels, Buttons, Areas */
  private JPanel serverAndPort,userAndPass, connectPanel, centerPanel, groupPanel,joinPanel, southPanel, buttons;
  private JButton login, logout, getMembers, createAccount, joinButton, createGroup, getTxt;
  private JTextArea chatroom, grouplist;
  
  /** if it is for connection */
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
    
    // The  with:
    connectPanel = new JPanel(new GridLayout(3,1));
    
    // Chat and Group Panel
    centerPanel = new JPanel(new BorderLayout());
    
    southPanel = new JPanel(new FlowLayout());
    
   
    // Server/Port
    serverAndPort = new JPanel(new GridLayout(1,5, 1, 3));
    tfServer = new JTextField(host);
    tfPort = new JTextField("" + port);
    
    tfPort.setHorizontalAlignment(SwingConstants.RIGHT);
    serverAndPort.add(new JLabel("Server Address:  "));
    serverAndPort.add(tfServer);
    
    serverAndPort.add(new JLabel("Port Number:  "));
    serverAndPort.add(tfPort);
    serverAndPort.add(new JLabel(""));


    //Username And Password
    userAndPass = new JPanel(new GridLayout(1,5, 1, 3));
    
    user = new JTextField("");
    userAndPass.add(user);
  
    userAndPass.add(new JLabel("Password:  "));
    pass = new JTextField("");
    userAndPass.add(pass);
    connectPanel.add(serverAndPort);
    connectPanel.add(userAndPass);

    
    
  
    // Chat Room
    chatroom = new JTextArea("Welcome to the Chat room\n", 80, 80);
    centerPanel.add(new JScrollPane(chatroom),BorderLayout.CENTER);
    chatroom.setEditable(false);
    
    //Group List
    grouplist = new JTextArea("Group Members\n");
    grouplist.setEditable(false);
    
    //Join Group
    joinButton = new JButton("Join Group: ");
    joinButton.addActionListener(this);
    joinText   = new JTextField("");
    joinText.setBackground(Color.WHITE);

    //Add Group Panel
    groupPanel = new JPanel(new BorderLayout());
    groupPanel.add(new JScrollPane(grouplist), BorderLayout.CENTER);
    joinPanel = new JPanel(new GridLayout(1,2));
    joinPanel.add(joinButton);
    joinPanel.add(joinText);
    groupPanel.add(joinPanel, BorderLayout.SOUTH);
    
    
    
    
    
    
    
 
    // the buttons
    
    //login
    login = new JButton("Login");
    login.addActionListener(this);
    
    //create
    createAccount = new JButton("Create Account");
    createAccount.addActionListener(this);
    
    // Create group
    createGroup = new JButton("Create Group");
    createGroup.addActionListener(this);
    createGroup.setEnabled(false);
    
    //getTxt
    getTxt = new JButton("GetTxt");
    getTxt.addActionListener(this);
    getTxt.setEnabled(false);
    
    
    // Logout
    logout = new JButton("Logout");
    logout.addActionListener(this);
    logout.setEnabled(false);       
    
    // GetMembers
    getMembers = new JButton("Get Members");
    getMembers.addActionListener(this);
    getMembers.setEnabled(false);      
      
     // Chatline 
    chatLine = new JTextField();
    
    // Add Buttons and Panels
    buttons = new JPanel();
    
    
    
    // Open Login Context
    openAllContext();
    
    
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
    grouplist.setCaretPosition(grouplist.getText().length() - 1);
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
    createGroup.setEnabled(false);
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
      
      // Toggle buttons
      logout.setEnabled(false);
      getMembers.setEnabled(false);
      getTxt.setEnabled(false);
      createGroup.setEnabled(false);
      
      //add login info
      add(southPanel);
      

      
      // disable the Server and Port JTextField
      tfServer.setEditable(true);
      tfPort.setEditable(true);
      
      
      
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
        chatLine.requestFocus();
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
       
     
      //clear password
      pass.setText("");
      
      // enable buttons
      logout.setEnabled(true);
      getMembers.setEnabled(true);
      getTxt.setEnabled(true);
      createGroup.setEnabled(true);
      
      //remove login info
      remove(southPanel);
      

      
      // disable the Server and Port JTextField
      tfServer.setEditable(false);
      tfPort.setEditable(false);
      
      // Action listener for when the user enter a message
      chatLine.setText("");
      chatLine.addActionListener(this);   
      chatLine.requestFocus();

    }
    
    
    
    
    if (o == joinButton){
      client.attemptGroupJoin(joinText.getText());
      
    }
  }
  
  private void openLoginContext(){
    
    add(connectPanel);
  }
  
  private void openAllContext(){
    

    // add Chat and Group Panel
    add(centerPanel, BorderLayout.CENTER);
    centerPanel.add(groupPanel, BorderLayout.EAST);
    
    add(connectPanel, BorderLayout.NORTH); 
    connectPanel.add(chatLine);

    
    buttons.add(login);
    buttons.add(logout);
    buttons.add(createAccount);
    buttons.add(createGroup);
    buttons.add(getMembers);
    buttons.add(getTxt);
    southPanel.add(buttons);
    add(southPanel, BorderLayout.SOUTH);


  }
  
  
  
 
  /**
   * run the gui
   * 
   */
  public static void main(String[] args) {
    new ClientGUI("localhost", 4444);
  }
    
}

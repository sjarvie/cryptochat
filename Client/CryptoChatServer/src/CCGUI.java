import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.border.MatteBorder;
import java.awt.Color;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class CCGUI {

	private JFrame frmCryptochat;
	private JTextField field_username;
	private JTextArea chatroom, memberlist;
	private JTextField chatline;
	private BorderLayout borderLayout;
	private JLayeredPane layeredPane;
	private JPanel login_panel, user_pass_panel, chat_panel;
	private JLabel lbl_username, lbl_password;
	
	 /** the default port number */
	  private String host;

	  private int port;
	  
	  /** the Client object */
	  private MultiThreadedClientCCGUI client;
	  
	  /** if it is for connection */
	  @SuppressWarnings("unused")
	  private boolean connected;
	  private JTextField field_password;
	  

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CCGUI window = new CCGUI("localhost", 4444);
					window.frmCryptochat.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public CCGUI(String host, int port) {
	    this.host = host;
	    this.port = port;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmCryptochat = new JFrame();
		borderLayout = (BorderLayout) frmCryptochat.getContentPane().getLayout();
		borderLayout.setVgap(11);
		frmCryptochat.setTitle("Crypto Chat");
		frmCryptochat.setBounds(100, 100, 450, 300);
		frmCryptochat.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		
		layeredPane = new JLayeredPane();
		frmCryptochat.getContentPane().add(layeredPane, BorderLayout.CENTER);
		
		
		login_panel = new JPanel();
		layeredPane.setLayer(login_panel, 2);
		login_panel.setBounds(0, 0, 450, 256);
		layeredPane.add(login_panel);
		login_panel.setLayout(null);
		
		
		
		user_pass_panel = new JPanel();
		user_pass_panel.setBorder(null);
		user_pass_panel.setBounds(87, 98, 255, 51);
		login_panel.add(user_pass_panel);
		user_pass_panel.setLayout(new GridLayout(2, 2, 0, 10));
		
		
		
		
		lbl_username = new JLabel("Username:");
		user_pass_panel.add(lbl_username);
		
		field_username = new JTextField();
		user_pass_panel.add(field_username);
		field_username.setColumns(10);
		
		lbl_password = new JLabel("Password:");
		user_pass_panel.add(lbl_password);
		
		field_password = new JTextField();
		user_pass_panel.add(field_password);
		field_password.setColumns(10);
		
		
		
		
		JTextArea textArea = new JTextArea();
		textArea.setBackground(UIManager.getColor("InternalFrame.borderColor"));
		textArea.setBounds(26, 191, 396, 59);
		login_panel.add(textArea);
		
		JButton btnNewButton = new JButton("Login");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				attemptLogin();
				
			}
		});
		btnNewButton.setBounds(69, 161, 113, 29);
		login_panel.add(btnNewButton);
		
		JButton btnCreateAccount = new JButton("Create Account");
		btnCreateAccount.setBounds(239, 161, 130, 29);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				attemptCreate();
				
			}
		});
		login_panel.add(btnCreateAccount);
		
		
		chat_panel = new JPanel();
		layeredPane.setLayer(chat_panel, 0);
		chat_panel.setBounds(0, 0, 450, 250);
		layeredPane.add(chat_panel);
		chat_panel.setLayout(new BorderLayout(10, 20));
		
	    chatroom = new JTextArea("", 80, 80);
		JScrollPane scrollPane = new JScrollPane(chatroom);
		chat_panel.add(scrollPane, BorderLayout.CENTER);
		
	    memberlist = new JTextArea("Group Members\n");
		JScrollPane member_panel = new JScrollPane(memberlist);
		chat_panel.add(member_panel, BorderLayout.EAST);
		
		chatline = new JTextField();
		chat_panel.add(chatline, BorderLayout.SOUTH);
		chatline.setColumns(20);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBackground(Color.LIGHT_GRAY);
		frmCryptochat.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		mnFile.setBackground(Color.LIGHT_GRAY);
		menuBar.add(mnFile);
		
		JMenuItem mntmSettings = new JMenuItem("Settings");
		mntmSettings.setBackground(Color.LIGHT_GRAY);
		mnFile.add(mntmSettings);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.setBackground(Color.LIGHT_GRAY);
		mnFile.add(mntmExit);
		
		JMenu mnGroup = new JMenu("Group");
		mnGroup.setBackground(Color.LIGHT_GRAY);
		menuBar.add(mnGroup);
		
		JMenuItem mntmCreateGroup = new JMenuItem("Create Group");
		mntmCreateGroup.setBackground(Color.LIGHT_GRAY);
		mnGroup.add(mntmCreateGroup);
		
		JMenuItem mntmSwitchGroup = new JMenuItem("Switch Group");
		mntmSwitchGroup.setBackground(Color.LIGHT_GRAY);
		mnGroup.add(mntmSwitchGroup);
		
		JMenuItem mntmInviteToGroup = new JMenuItem("Add To Group");
		mntmInviteToGroup.setBackground(Color.LIGHT_GRAY);
		mnGroup.add(mntmInviteToGroup);
	}
	
	  protected void attemptCreate() {
		//Dont submit empty queries
	      String username = field_username.getText().trim();
	      String password = field_password.getText().trim();
	      if((username.length() == 0) || (password.length() == 0)){
	          return;
	      }

	     
	      
	      // try creating a new Client with GUI
	      client = new MultiThreadedClientCCGUI(host,port,this);       
	      
	      if(!client.connect()){
	        return;
	      }
	      client.attemptCreate(username,password);
	      client.disconnect();
	      return;
		
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
    memberlist.append(str);
    memberlist.setCaretPosition(chatroom.getText().length() - 1);
  }
  
  /**
   * Clears the group for refreshing list
   */
  void clearGroup(){
	  memberlist.setText("");
  }

	protected void attemptLogin() {
		
		  String username = field_username.getText().trim();
	      String password = field_password.getText().trim();
	      System.out.println("User : " +username + password);
	      
	      if((username.length() == 0) || (password.length() == 0)) {
	          return;
	        }
	      
	     
	      
	      // try creating a new Client with GUI
	      client = new MultiThreadedClientCCGUI(host,port,this);
	      
	       if(!client.connect()){
	           return;
	       }
	       //attempt to login
	       if(!client.attemptLogin(username,password)){
	           client.disconnect();
	           return;
	       }    
	       
	       //Connect Passed
	       connected = true;
	       
	       //clear password
	       field_password.setText("");
	       
	       
	       //bring chat layer to front
			layeredPane.setLayer(login_panel, 0);
			layeredPane.setLayer(chat_panel, 1);
	       
		
	}
	
	/**
	   *  notify the user that the connection has failed
	   *  
	   */
	  void connectionFailed() {
	   
	    
	    
	    connected = false;
	  }
}

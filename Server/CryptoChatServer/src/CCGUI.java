import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
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
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.border.BevelBorder;


public class CCGUI extends JFrame {

	private JTextField field_username, server_field, port_field;
	private JTextArea chatroom, member_list, login_text, group_list;
	private JTextField chatline, add_to_group_field, create_group_field, remove_from_group_field, switch_to_group_field;
	private BorderLayout borderLayout;
	private JLayeredPane layeredPane;
	private JPanel login_panel, user_pass_panel, chat_panel, server_port_panel, group_panel, group_button_panel;
  
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
	  private JPanel login_button_panel;
	  private JPanel chatline_panel;
	  

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CCGUI window = new CCGUI("localhost", 4444);
					window.setVisible(true);
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
		//borderLayout = (BorderLayout) getContentPane().getLayout();
		//borderLayout.setVgap(11);
		
		//setResizable(false);
		setTitle("Crypto Chat");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		
		
		
		//getContentPane().add(layeredPane, BorderLayout.CENTER);
		
		
		login_panel = new JPanel();
		//layeredPane.setLayer(login_panel, 0);
		login_panel.setSize(450, 300);
		//layeredPane.add(login_panel);
		login_panel.setLayout(new GridLayout(3, 1, 0, 0));
		
		
		
		user_pass_panel = new JPanel();
    user_pass_panel.setLayout(new GridLayout(4, 2, 0, 0));
		user_pass_panel.setBorder(null);
		login_panel.add(user_pass_panel);
		
		
		lbl_username = new JLabel("Username:");
		lbl_username.setSize(40, 20);
		user_pass_panel.add(lbl_username);
		
		field_username = new JTextField();
		user_pass_panel.add(field_username);
		field_username.setColumns(10);
		
		lbl_password = new JLabel("Password:");
		user_pass_panel.add(lbl_password);
		
		field_password = new JTextField();
		user_pass_panel.add(field_password);
		field_password.setColumns(10);
		
		login_button_panel = new JPanel();
		login_panel.add(login_button_panel);
		login_button_panel.setLayout(new GridLayout(2, 2, 0, 0));
		
		
		//Login
		JButton btnNewButton = new JButton("Login");
		login_button_panel.add(btnNewButton);
		
		
		//Create Account
		JButton btnCreateAccount = new JButton("Create Account");
		login_button_panel.add(btnCreateAccount);
		btnCreateAccount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				attemptCreate();
				
			}
		});
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				attemptLogin();
				
			}
		});
		
		
		
		//login text
		login_text = new JTextArea();
		login_text.setBackground(UIManager.getColor("InternalFrame.borderColor"));
		login_text.setLineWrap(true);
		login_text.setWrapStyleWord(true);
		login_panel.add(login_text);
		
		
		
		
		// Chat Panel
		chat_panel = new JPanel(new BorderLayout(10,20));
		chat_panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		
		
		chat_panel.setSize(720, 540);
		//layeredPane.add(chat_panel);
		
	  chatroom = new JTextArea("", 80, 80);
		JScrollPane scrollPane = new JScrollPane(chatroom);
		chat_panel.add(scrollPane, BorderLayout.CENTER);
		
		//Member Panel
	  member_list = new JTextArea("Group Members\n");
	  member_list.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		JScrollPane member_panel = new JScrollPane(member_list);
		member_panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		chat_panel.add(member_panel, BorderLayout.EAST);
		
		
		//Chat Line
    chatline_panel = new JPanel();
    chatline_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
    chatline_panel.add(new JLabel("Chat: "));
		chatline = new JTextField();
		chatline.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		chatline_panel.add(chatline);
		chatline.setColumns(40);
		chat_panel.add(chatline_panel, BorderLayout.SOUTH);
		
		
		// Menu
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBackground(Color.LIGHT_GRAY);
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		mnFile.setBackground(Color.LIGHT_GRAY);
		menuBar.add(mnFile);
		
		JMenuItem mntmSettings = new JMenuItem("Settings");
		mntmSettings.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent arg0) {
		    if(!connected){
		      openServerPortContext();
		    }
		    
		  }
		});
		mntmSettings.setBackground(Color.LIGHT_GRAY);
		mnFile.add(mntmSettings);
		
		//Exit
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.setBackground(Color.LIGHT_GRAY);
		mntmExit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        client.disconnect();
        System.exit(0);
      }
    });
		mnFile.add(mntmExit);
		
		
		//Group
		JMenu mnGroup = new JMenu("Group");
		mnGroup.setBackground(Color.LIGHT_GRAY);
		menuBar.add(mnGroup);
		
		JMenuItem mntmCreateGroup = new JMenuItem("Manage Groups");
		mntmCreateGroup.setBackground(Color.LIGHT_GRAY);
		mnGroup.add(mntmCreateGroup);
		mntmCreateGroup.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        openGroupContext();
      }
    });
    
	
		group_panel = new JPanel(new BorderLayout());
		
		group_list = new JTextArea("My Groups \n");
		group_list.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		
		//Group buttons
		group_button_panel = new JPanel(new GridLayout(4,2,4,4));
		JButton create_group_button = new JButton("Create Group");
		create_group_field = new JTextField("");
    create_group_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        String group_id = create_group_field.getText().trim();
        
      }
    });
    
    group_button_panel.add(create_group_button);
    group_button_panel.add(create_group_field);
    
    JButton add_to_group_button = new JButton("Add to Group");
    add_to_group_field = new JTextField("");
    add_to_group_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        String user_id = add_to_group_field.getText().trim();
      }
    });
    
    group_button_panel.add(add_to_group_button);
    group_button_panel.add(add_to_group_field);
    
    
    JButton switch_to_group_button = new JButton("Switch to Group");
    switch_to_group_field = new JTextField("");
    switch_to_group_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        String group_id = switch_to_group_field.getText().trim();
      }
    });
    
    group_button_panel.add(switch_to_group_button);
    group_button_panel.add(switch_to_group_field);
    
    JButton remove_from_group_button = new JButton(" Remove From Group");
    remove_from_group_field = new JTextField("");
    remove_from_group_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        String user_id = remove_from_group_field.getText().trim();
      }
    });
    
    group_button_panel.add(remove_from_group_button);
    group_button_panel.add(remove_from_group_field);
    
    
    JPanel manage_group_content_panel = new JPanel(new GridLayout(1,2, 0, 20));
    manage_group_content_panel.add(group_list);
    manage_group_content_panel.add(member_list);

    
		group_panel.add(manage_group_content_panel, BorderLayout.CENTER);
		group_panel.add(group_button_panel, BorderLayout.SOUTH);
		
		// Server/Port
    server_port_panel = new JPanel(new GridLayout(3,2, 1, 3));
    server_field = new JTextField(host);
    port_field = new JTextField("" + port);
    
    server_port_panel.add(new JLabel("Server Address:  "));
    server_port_panel.add(server_field);
    
    server_port_panel.add(new JLabel("Port Number:  "));
    server_port_panel.add(port_field);
    
		
    JButton server_port_save_button = new JButton("Save");
    server_port_save_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        host = server_field.getText().trim();
        port = Integer.parseInt(port_field.getText().trim());
        openLoginContext();
      }
    });
    
    JButton server_port_cancel_button = new JButton("Cancel");
    server_port_cancel_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        openLoginContext();
      }
    });
		
    
    server_port_panel.add(server_port_save_button);
    server_port_panel.add(server_port_cancel_button);

		
		openLoginContext();
		
		
	}
	
	
	
	
	
	/**
	 * Send a create request to server
	 */
  private void attemptCreate() {
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
  void appendMembers(String str) {
    member_list.append(str);
    member_list.setCaretPosition(member_list.getText().length() - 1);
  }
  
  /**
   * Called by the Client to append text in the TextArea
   * @param str
   */
  void appendGroups(String str) {
    group_list.append(str);
    group_list.setCaretPosition(group_list.getText().length() - 1);
  }
  
  /**
   * Clears the group for refreshing list
   */
  void clearMembers(){
	  member_list.setText("");
  }
  
  /**
   * Clears the group for refreshing list
   */
  void clearGroups(){
    member_list.setText("");
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
	       
	   
     openChatContext();


	       
		
	}
	
	private void openServerPortContext(){
    //resize window
     setSize(450, 320);
     
     //switch panels
     add(server_port_panel);
     remove(login_panel);
     remove(chat_panel);
     remove(group_panel);

    
	}
	
	
	private void openLoginContext(){
	  
    
    setLocation(200,200);
    setSize(450,300);
    
    //switch panels
    remove(server_port_panel);
    add(login_panel);
    remove(chat_panel);
    remove(group_panel);

	}
	
	
	
	private void openChatContext() {
    
    //resize window
     setSize(800, 600);
     
     //switch panels
     remove(server_port_panel);
     remove(login_panel);
     remove(group_panel);
     add(chat_panel);
    
  }
	
	

  private void openGroupContext() {
    //resize window
     setSize(800, 600);
     
     //switch panels
     remove(server_port_panel);
     remove(login_panel);
     remove(chat_panel);
     add(group_panel);
    
  }
	/**
	   *  notify the user that the connection has failed
	   *  
	   */
	  void connectionFailed() {
	    connected = false;
	  }

	/**
	 * 
	 * @param msg
	 */
  public void displayLogin(String msg) {
    login_text.setText(msg + "\n");
  }
  
  
  
}

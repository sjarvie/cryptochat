import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;


/** 
 * Testbench for Login Functionality
 */
public class SimpleLoginTest {
	
  /**
  * Run the test
  */
  public static void main(String args[]) {
    int choice = -1;
  
    // Poll the user to interact with server login/creation 
    while(choice != 0) {
    
      String usrName = null, pwd = null;
      UserManager um = new UserManager();
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

      System.out.println("What do you want?\n\t1.Sign up\n\t2.Sign in\n\t0.Exit");
    
      try {
        choice = Integer.parseInt(br.readLine());
      }
      catch (NumberFormatException e) {
        e.printStackTrace();
      }
      catch (IOException e) {
        System.out.println("Please input 1 or 2.");
      }
    
      switch(choice) {
		
		    // Exit the prompt.
		    case 0:
		      System.out.println("Bye!");
		      break;
		  
		    // Test account creation.
		    case 1:
		      um.initUserTable();
		      System.out.println("Sign up:");
		      System.out.println("Please input your user name");
		      br = new BufferedReader(new InputStreamReader(System.in));
		      try {
		        usrName = br.readLine();
		      } 
		      catch (IOException e) {}
			
		      System.out.println("Please input your password");
		      br = new BufferedReader(new InputStreamReader(System.in));
		      try {
		        pwd = br.readLine();
		      }
		      catch (IOException e) {}
			
			
		      um.createAccount(usrName, pwd);
		      break;
	
		    // Test account login.
		    case 2:
		    
		      System.out.println("Please Input User Name:");
		      br = new BufferedReader (new InputStreamReader(System.in)); 
		      try {
		        usrName = br.readLine();
		      } 
		      catch (IOException e) {}
			
		      System.out.println("Please Input Your Password: ");
		      
		      br = new BufferedReader (new InputStreamReader(System.in));
		      try {
		        pwd = br.readLine();
		      } 
		      catch (IOException e) {}
		      
		      if(um.LoginTest(usrName, pwd)){
		        System.out.println("Login succed!");
		        System.out.println("To change a new password, Please in put your username");
		        try {
		          br = new BufferedReader (new InputStreamReader(System.in));
		          String usr = br.readLine();
		          br = new BufferedReader (new InputStreamReader(System.in));
		          String opwd = br.readLine();
		          br = new BufferedReader (new InputStreamReader(System.in));
		          String npwd = br.readLine();
	          
		          if(um.changePassword(usr, opwd, npwd)) {
		            System.out.println("OK!");
		          }
		        } 
		        catch (IOException e){
		          e.printStackTrace();
		        }
		      }
		      else {
		        System.out.println("Login failed!");
		      }
		      break;
		    
		    //Test a group.
		    case 3:
			  
		      um.addGroupMembership("abc", "Chinese");
				
		      ArrayList<String> g = um.getGroupMembership("abc");
		      for(int i = 0; i<g.size(); i++) {
		        System.out.println(g.get(i));
		      }
		      break;
			
		    // Test a group manager
		    case 4: 
			  
		      GroupManager gm = new GroupManager();
		      Group group = new Group();
		      group.createGroup("abc");
				
		      gm.initGroupTable();
		      gm.createANullGroup(group.getGroupID(), group.getOwnerName());
				
		      ArrayList<String> g2 = gm.getMembers(group.getGroupID());
		      for(int i = 0; i<g2.size(); i++) {
		        System.out.println(g2.get(i));
		      }
				
		      System.out.println( gm.getJoinTime("abc", group.getGroupID()).toString());
		      Recorder.initialFile(group.getGroupID());
		      break;
				
		    // Test the recorder.
		    case 5:
			  
		      Date cur = new Date(System.currentTimeMillis());
		      Recorder.Record("abc20130427164510", cur, "abc", "hello, world");
				
		      ArrayList<String> g3 = Recorder.getNextRecords("abc20130427164510", cur);
		      for(int i = 0; i<g3.size(); i++) {
		        System.out.println(g3.get(i));
		      }
				
		      System.out.println(""+Recorder.getFileSize("abc20130427164510"));
		      Recorder.removeFirstLine("abc20130427164510");
		      break;
        }
      }
    }
}

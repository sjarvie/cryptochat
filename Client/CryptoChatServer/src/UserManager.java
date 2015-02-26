import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Manage a user's information storage and retrieval
 *
 */
public class UserManager {
	
  /** Synchronization primitives */
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
  private final Lock rl = rwl.readLock();
  private final Lock wl = rwl.writeLock();
  
  
	/**
	 * Write user information to file
	 * @param doc
	 */
  private void saveUserInfo(Document doc) {
    
    // Atomic Write
		wl.lock();
		try {
		  
		  // Open File for write
      StreamResult result = new StreamResult(new File("userinfo.xml"));

      
      // Edit DOM structure with user information
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer;
			transformer = transformerFactory.newTransformer();
			
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);
			
		} 
		catch (TransformerException e) {
			e.printStackTrace();
		}
		finally {
			wl.unlock();
		}
	}
  
	/**
	 * Retrieve user XML document
	 * 
	 * @return     the document or null
	 */
  private Document getUserInfo() {
    
    //Atomic Read
  	rl.lock();
  	Document d = null;
		try {
			d = XmlManager.parse("userinfo.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			rl.unlock();
		}
		return d;
  }
    
  /**
   * Initialize a new user table
   * 
   * @return      true if new table added or exists
   *              false if failure
   */
	public boolean initUserTable() {
	  
		//if file already existed, do nothing.
		if ( !(new File("userinfo.xml").exists())) {
		
  		try {
  			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
  			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
  			
  			// initial root elements
  			Document doc = docBuilder.newDocument();
  			Element rootElement = doc.createElement("table");
  			doc.appendChild(rootElement);
  			
  			// write the content into xml file
  			saveUserInfo(doc);
  		}
  		catch(Exception e){
  		  e.printStackTrace();
  		  return false;
  		}
  		
		}
		System.out.println("File Initialized!");
    return true; 
	}
	
	
	
	/**
	 * test if a password format is valid and return a message for client
	 * @param   pwd			password to be tested
	 * 
	 * @return	     null		valid
	 * 			         message		the reason why password is invalid
	 */
	private String testPassword(String pwd, String username) {
	  
		String message = "";
		
		// Test against username
	  if(username.equals(pwd)) {
	    return "You should not use a password which is the same with your user name.";
    }
		
		// Test length
		String patternStr = "(\\w|\\p{Graph}){8,24}";
		if(!Pattern.matches(patternStr, pwd))
			message += "Password should be longer than 8 characters and shorter than 24 characters.\n";
		
		// Test upper case
		patternStr = ".*?[A-Z]+.*?";
		if(!Pattern.matches(patternStr, pwd)) {
			message += "Password must contain at least 1 uppercase\n";
		}
		
		// Test lower case
		patternStr = ".*?[a-z]+.*?";
		if(!Pattern.matches(patternStr, pwd)) {
			message += "Password must contain at least 1 lowercase\n";
		}
		
		// Test numeric
		patternStr= ".*?[0-9]+.*?";
		if(!Pattern.matches(patternStr, pwd)) {
			message += "Password must contain at least 1 number\n";
		}
		
		return message.trim();
	}
	
	
	/**
	 * Test whether a user name already exists 
	 * 
	 * @param   usrName	    the name to be tested
	 * @return	            true        existed
	 * 			                false 		not existed
	 */
	public boolean usernameExists(String usrName) {
		
		
		Element existElement = null;
		
		// Retrieve element if possible and test
		try {
			Document d = getUserInfo();
			Element tableElement = (Element)(d.getElementsByTagName("table").item(0));
			existElement = XmlManager.getChildElement(tableElement, "User", "name", usrName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return (existElement != null);

	}
	
	/**
	 * Add a group membership to a user xml file
	 * 
	 * @param   UserName	 the name of that user
	 * @param   GroupID	   the id of that group
	 * @return	           whether or not the addition succeeded
	 */
	public boolean addGroupMembership(String userName, String groupID) {
		
	  //TODO: Test whether this group exists in xml file
		/*GroupManager gm = new GroupManager();
		
		if(!gm.groupIDExists(groupID)) {
			System.out.println("group doesn't exist: " + groupID);
			return false;
		}
		*/
	  
		// Test if user exists
		try {
			if(!usernameExists(userName)) {
				System.out.println("User: "+ userName+" doesn't exist!");
				return false;
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// Test whether already exists in group
		if(isMemberOf(userName,groupID)) {
			return true;
		}
		
		
		// Add membership
    Document d = getUserInfo();
		Element tableElement = (Element)(d.getElementsByTagName("table").item(0));
		
		try {
		  
			//TODO: Whether we should encrypt the group string here?
			Element userElement = XmlManager.getChildElement(tableElement, "User", "name", userName);
			
			//String groupString = userElement.getTextContent();
			//groupString = groupString + groupID +";";
			//userElement.setTextContent(groupString);
			
			Element groupElement = d.createElement("Group");
			groupElement.setAttribute("id", groupID);
			userElement.appendChild(groupElement);
			saveUserInfo(d);
			return true;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	// Test if a user if a member has the record of the membership of a group
	public boolean isMemberOf(String userName, String GroupID)
	{
		boolean isThere = false;
		ArrayList<String> g = getGroupMembership(userName);
		for(int i = 0; i < g.size(); i++)
		{
			if(g.get(i).equals(GroupID))
			{
				isThere = true;
				break;
			}
		}
		return isThere;
	}
	
	/**
	 * get a user's membership
	 * @param   UserName	the name of that user
	 * @return	a list of strings denotes all the groups the user is in
	 */
	public ArrayList<String> getGroupMembership(String userName) {
	  
		try {
			if(!usernameExists(userName)){
			  return null;
			}
		} 
		catch (Exception e){
			e.printStackTrace();
		}
		
		
		ArrayList<String> groups = new ArrayList<String>();

    //add membership
		Document d = getUserInfo();
		Element tableElement = (Element)(d.getElementsByTagName("table").item(0));
		
		try {
			//TODO: Whether we should encrypt the group string here?
			Element userElement = XmlManager.getChildElement(tableElement, "User", "name", userName);
			
			
			// Add each group
			for (int i= 0; i< userElement.getElementsByTagName("Group").getLength(); i++) {
				Element groupElement = (Element) userElement.getElementsByTagName("Group").item(i);
				groups.add(groupElement.getAttribute("id"));
			}
			
			return groups;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	/**
	 * Create an account with the given name and password 
	 * @param   usrName	    the user name
	 * 		    pwd	        the password
	 * @return	true        succeed
	 * 			false 		failed
	 */
	public boolean createAccount(String username, String pwd) {
	  
		// Test Password
		String message = testPassword(pwd, username);
		if(message != "") {
			System.out.println(message);
			return false;
		}
		
		//test whether user name already existed. 
		try {
			if( usernameExists(username)) {
				System.out.println("\t"+username+" Already exists!"+" plz pick a new one!");
				System.out.println("\tUser Creation Failed");
				return false;
			}	
		}
		catch(Exception e) {
			System.out.println(""+e.toString());
		}
		
		Document d = getUserInfo();
		
		// Salt password
		pwd = PasswordHandler.createHash(pwd);
		
		// User elements
		Element UserNode = d.createElement("User");
		UserNode.setAttribute("name", username);
		UserNode.setAttribute("pass", pwd);		
		((Element)d.getElementsByTagName("table").item(0)).appendChild(UserNode);
		
		System.out.println("\tnew user: [" +username+ "] saved with the passwrd ["+pwd + "]");
		
		// Save User
		try {
			saveUserInfo(d);
		}
		catch(Exception e) {
			System.out.println(e.toString());
			return false;
		}
		
	  // Create Group for new user
    Group G = new Group();
    G.createGroup(username);
    
    return true;
	}
	
	/**
	 * Test login user 
	 * 
	 * @param  usrName	    the user name
	 * @param  pwd	        the password
	 * @return              the success of the operation
	 */
	public boolean LoginTest(String usrName, String pwd) {	
		//test whether user name already existed. 
		try {
			if( !usernameExists(usrName)) {
				System.out.println("\tUser Name doesn't existed!");
				return false;
			}	
		
		
			//Get XML
			Document d = getUserInfo();
	
			//Fetch element and test
			Element tableElement = (Element)(d.getElementsByTagName("table").item(0));
		
			Element userElement = XmlManager.getChildElement(tableElement, "User", "name", usrName);
			String storedPass = userElement.getAttribute("pass");
			if(PasswordHandler.validatePassword(pwd, storedPass)){
				return true;
			} 
	  }
		catch (Exception e) {
      e.printStackTrace();
    }
		return false;
	}

	/**
	 * Update password for an account
	 * 
	 * @param usrName	      	the user name
	 * @param password	  	  the current password
	 * @param newPassword   	the new password
	 * @return	              whether or not the operation succeeded
	 */
	public boolean changePassword(String username, String password, String new_password) {
	  
		String message = testPassword(new_password, username);
		if(message != null) {
			System.out.println(message);
			return false;
		}
		
		// Authenticate user and change password
		if(LoginTest(username, password)) {
			Document d = getUserInfo();
			try {
			  
	      //find the proper user element
				Element tableElement = (Element)(d.getElementsByTagName("table").item(0));
				Element userElement = null;
				userElement = XmlManager.getChildElement(tableElement, "User", "name", username);
				
        //change the password
				new_password = PasswordHandler.createHash(new_password);				
				userElement.setAttribute("pass", new_password);
				
			} 
			catch (Exception e){
				e.printStackTrace();
				return false;
			}
			
			// write the content into xml file
			try {	
				saveUserInfo(d);
				return true;
			}
			catch(Exception e) {
        e.printStackTrace();
			}
		}
		return false;
	}
	
	
	//TODO: delete group membership of a user
	public void deleteMmbership(String userName, String groupID) {
		try {
			if(!usernameExists(userName))
				return;
			//Get XML
			Document d = getUserInfo();	
			//add membership
			Element tableElement = (Element)(d.getElementsByTagName("table").item(0));
			Element userElement = XmlManager.getChildElement(tableElement, "User", "name", userName);
			Element groupElement = XmlManager.getChildElement(tableElement, "Group", "id", groupID);
			userElement.removeChild(groupElement);
			saveUserInfo(d);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	//TODO: delete user
	public void deleteUser(String userName)
	{
		try
		{
			if(!usernameExists(userName))
				return;
			
			//Get XML
			Document d = getUserInfo();
					
			//add membership
			Element tableElement = (Element)(d.getElementsByTagName("table").item(0));
			Element userElement = XmlManager.getChildElement(tableElement, "User", "name", userName);
			tableElement.removeChild(userElement);
			
			saveUserInfo(d);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
}

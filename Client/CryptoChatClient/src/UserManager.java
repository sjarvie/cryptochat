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
//import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



/**
 * 
 * @author sjarvie
 *
 */
public class UserManager 
{
	/**
	 * initialize the xml file storing user info (include name and password for now)
	 * @return   true     file initialized
	 *           false    file existed, do not need to initialize it
	 */
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock rl = rwl.readLock();
    private final Lock wl = rwl.writeLock();
	
    private void saveUserInfo(Document doc)
	  {
		wl.lock();
		try
		{
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer;
			transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("userinfo.xml"));
			transformer.transform(source, result);
		} catch (TransformerException e)
		{
			e.printStackTrace();
		}
		finally
		{
			wl.unlock();
		}
	}
	
    private Document getUserInfo()
    {
    	rl.lock();
    	Document d = null;
		try
		{
			d = XmlManager.parse("userinfo.xml");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			rl.unlock();
		}
		return d;
    }
    
    
	public boolean initUserTable() 
	{
		//if file already existed, do nothing.
		if(new File("userinfo.xml").exists())
		{
			System.out.println("File Initialized!\n");
			return false;  //need not to initialize
		}
		try
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			// initial root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("table");
			doc.appendChild(rootElement);
			// write the content into xml file
			saveUserInfo(doc);
		}
		catch(Exception e){}
		System.out.println("File Initializing......");
		System.out.println("File Initialized!");
		return true; //initialized
	}
	
	
	
	/**
	 * test if a password format is valid
	 * @param   pwd			password to be tested
	 * 
	 * @return	null		valid
	 * 			message		the reason why password is invalid
	 */
	private String testPassword(String pwd)
	{
		String message = "";
		//test length
		String patternStr = "(\\w|\\p{Graph}){8,24}";
		if(!Pattern.matches(patternStr, pwd))
			message += "Password should be longer than 8 characters and shorter than 24 characters.\n";
		
		//test upper case
		patternStr = ".*?[A-Z]+.*?";
		if(!Pattern.matches(patternStr, pwd))
			message += "Password must contain at least 1 uppercase\n";
		
		patternStr = ".*?[a-z]+.*?";
		if(!Pattern.matches(patternStr, pwd))
			message += "Password must contain at least 1 lowercase\n";
		
		patternStr= ".*?[0-9]+.*?";
		if(!Pattern.matches(patternStr, pwd))
			message += "Password must contain at least 1 number\n";
		return message.trim();
	}
	
	
	/**
	 * test whether a user name already existed 
	 * @param   usrName	    the name to be tested
	 * @return	true        existed
	 * 			false 		not existed
	 */
	public boolean usernameExists(String usrName)
	{
		//Document d = null;
		//d = XmlManager.parse("userinfo.xml");
		
		Element existElement = null;
		try {
			Document d = getUserInfo();
			Element tableElement = (Element)(d.getElementsByTagName("table").item(0));
			existElement = XmlManager.getChildElement(tableElement, "User", "name", usrName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(existElement != null)
			return true;
		return false; 
	}
	
	/**
	 * add a group membership to a user xml file
	 * @param   UserName	the name of that user
	 * @param   GroupID	    the id of that group
	 * @return	true        succeed
	 * 			false 		failed
	 */
	public boolean addGroupMembership(String userName, String groupID)
	{
		//TODO: Test whether this group exists in xml file
		GroupManager gm = new GroupManager();
		/*
		if(!gm.groupIDExists(groupID))
		{
			System.out.println("group doesn't exist: " + groupID);
			return false;
		}
		*/
		//if not exists, return false
		//else
		
		//test if user exists
		try
		{
			if(!usernameExists(userName))
			{
				System.out.println("User: "+ userName+" doesn't exist!");
				rl.unlock();
				return false;
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		//TODO: Test whether already exists, if yes return true and do nothing
		if(isMemberOf(userName,groupID))
		{
			return true;
		}
		
		//Get XML
		Document d = getUserInfo();
		
		//add membership
		Element tableElement = (Element)(d.getElementsByTagName("table").item(0));
		try 
		{
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
		catch (Exception e) 
		{
			e.printStackTrace();
			return false;
		}
	}
	
	//test if a user if a member has the record of the membership of a group
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
	public ArrayList<String> getGroupMembership(String userName)
	{
		try
		{
			if(!usernameExists(userName))
				return null;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		ArrayList<String> groups = new ArrayList<String>();
		
		Document d = getUserInfo();
		//add membership
		Element tableElement = (Element)(d.getElementsByTagName("table").item(0));
		try 
		{
			//TODO: Whether we should encrypt the group string here?
			Element userElement = XmlManager.getChildElement(tableElement, "User", "name", userName);
			//String groupString = userElement.getTextContent();
			//String[] params = groupString.split(";");
			for(int i= 0; i< userElement.getElementsByTagName("Group").getLength(); i++)
			{
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
	public boolean createAccount(String usrName, String pwd)
	{
		if(usrName.equals(pwd))
		{
			System.out.println("/tYou should not use a password which is the same with your uer name.");
			return false;
		}
		String message = testPassword(pwd);
		if(!message.equals(""))
		{
			System.out.println(message);
			return false;
		}
		//test whether user name already existed. 
		try
		{
			if( usernameExists(usrName) )
			{
				System.out.println("\t"+usrName+" Already exists!"+" plz pick a new one!");
				System.out.println("\tUser Creation Failed");
				return false;
			}	
		}
		catch(Exception e)
		{
			System.out.println(""+e.toString());
		}
		
		Document d = getUserInfo();
		
		
		// salt password
		pwd = PasswordHandler.createHash(pwd);
		
		// user elements
		Element UserNode = d.createElement("User");
		UserNode.setAttribute("name", usrName);
		UserNode.setAttribute("pass", pwd);		
		
		((Element)d.getElementsByTagName("table").item(0)).appendChild(UserNode);
		System.out.println("\tnew user: [" +usrName+ "] saved with the passwrd ["+pwd + "]");
		
		// write the content into xml file
		try
		{	
			saveUserInfo(d);
			return true;
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
			return false;
		}
	}
	
	/**
	 * Test login user 
	 * @param   usrName	    the user name
	 * 		    pwd	        the password
	 * @return	true        login successfully
	 * 			false 		login failed
	 */
	public boolean LoginTest(String usrName, String pwd)
	{	
		//test whether user name already existed. 
		try
		{
			if( !usernameExists(usrName) )
			{
				System.out.println("\tUser Name doesn't existed!");
				return false;
			}	
		}
		catch(Exception e)
		{
			System.out.println(""+e.toString());
		}
		
		//Get XML
		Document d = getUserInfo();
	
		//Fetch element and test
		Element tableElement = (Element)(d.getElementsByTagName("table").item(0));
		try 
		{
			Element userElement = XmlManager.getChildElement(tableElement, "User", "name", usrName);
			String storedPass = userElement.getAttribute("pass");
			if(PasswordHandler.validatePassword(pwd, storedPass))
				return true;
		
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		 return false;
	}

	/**
	 * Update password for an account
	 * Note that: previous password is required to update for safety concern 
	 * @param   usrName	      	the user name
	 * 		    password	  	the password
	 *          newPassword   	the new password
	 * @return	true        	succeed
	 * 			false 			failed
	 */
	public boolean changePassword(String userName, String password, String newPassword)
	{
		String message = testPassword(newPassword);
		if(!message.equals(""))
		{
			System.out.println(message);
			return false;
		}
		
		//authenticating the user
		if(LoginTest(userName, password))
		{
			//if authenticated
			Document d = getUserInfo();
			try 
			{
				Element tableElement = (Element)(d.getElementsByTagName("table").item(0));
				Element userElement = null;
				//find the proper user element
				userElement = XmlManager.getChildElement(tableElement, "User", "name", userName);
				
				//salt the new password
				newPassword = PasswordHandler.createHash(newPassword);
				
				//change the password
				userElement.setAttribute("pass", newPassword);
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				return false;
			}
			
			// write the content into xml file
			try
			{	
				saveUserInfo(d);
				return true;
			}
			catch(Exception e)
			{
				System.out.println(e.toString());
				return false;
			}
		}
		else
			return false;
	}
	
	
	//TODO: delete group membership of a user
	public void deleteMmbership(String userName, String groupID)
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

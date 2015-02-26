import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class GroupManager
{
	
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock rl = rwl.readLock();
    private final Lock wl = rwl.writeLock();
    
    private final DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
    
	public boolean initGroupTable() 
	{
		//if file already existed, do nothing.
		if(new File("group.xml").exists())
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
			saveGroupInfo(doc);
		}
		catch(Exception e){}
		System.out.println("File Initializing......");
		System.out.println("File Initialized!");
		return true; //initialized
	}
	
	
	private void saveGroupInfo(Document doc)
	{
		wl.lock();
		try
		{
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer;
			transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("group.xml"));
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
	
	private Document getGroupInfo()
    {
    	rl.lock();
    	Document d = null;
		try
		{
			d = XmlManager.parse("group.xml");
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
	
	public void createANullGroup(String groupID, String ownerName)
	{
		initGroupTable();
		if(groupIDExists(groupID))
			return;
		//else
		Document d = getGroupInfo();
		
		Element groupNode = d.createElement("Group");
		groupNode.setAttribute("id", groupID);
		groupNode.setAttribute("owner", ownerName);	
		((Element)d.getElementsByTagName("table").item(0)).appendChild(groupNode);
		saveGroupInfo(d);
		
		//addMember(ownerName, groupID);
	}
	
	
	/**
	 * test whether a group id already existed 
	 * @param   usrName	    the name to be tested
	 * @return	true        existed
	 * 			false 		not existed
	 */
	public boolean groupIDExists(String groupID)
	{
		//Document d = null;
		//d = XmlManager.parse("userinfo.xml");
		
		Document d = getGroupInfo();
		Element tableElement = (Element)(d.getElementsByTagName("table").item(0));
		Element existElement = null;
		try
		{
			existElement = XmlManager.getChildElement(tableElement, "Group", "id", groupID);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		if(existElement != null)
			return true;
		return false; 
	}
	
	/**
	 * add a user to a group 
	 * @param   usrName	    the name to be added
	 * @param   groupID	    the id of that group
	 */
	public boolean addMember(String userName, String groupID)
	{
		if(!groupIDExists(groupID))
		{
			System.out.println("group doesn't exist: " + groupID);
			return false;
		}
		//test if user exists
		UserManager um = new UserManager();
		try
		{
			if(!um.usernameExists(userName))
			{
				System.out.println("User: "+ userName+" doesn't exist!");
				return false;
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		//DONE:  Test whether already exists, if yes return true and do nothing
		if(isMemberOf(userName,groupID))
		{
			return true;
		}
		
		//Get XML
		Document d = getGroupInfo();
		
		//add membership
		Element tableElement = (Element)(d.getElementsByTagName("table").item(0));
		try 
		{
			//TODO: Whether we should encrypt the group string here?
			Element groupElement = XmlManager.getChildElement(tableElement, "Group", "id", groupID);
			//String memberString = userElement.getTextContent();
			//memberString = memberString + userName +";";
			//userElement.setTextContent(memberString);
			Element memberElement = d.createElement("User");
			memberElement.setAttribute("name", userName);
			Date curTime = new Date(System.currentTimeMillis());
			memberElement.setAttribute("time",format.format(curTime));
			groupElement.appendChild(memberElement);
			saveGroupInfo(d);
			//add this membership to that user xml file, too
			um.addGroupMembership(userName, groupID);
			return true;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public void deleteMember(String userName, String groupID)
	{
		if(!groupIDExists(groupID))
		{
			return;
		}
		//test if user exists
		UserManager um = new UserManager();
		try
		{
			if(!um.usernameExists(userName))
			{
				return;
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		//DONE:  Test whether membership exists. if no, and do nothing
		if(!isMemberOf(userName,groupID))
		{
			return;
		}
		
		//Get XML
		Document d = getGroupInfo();
		
		//add membership
		Element tableElement = (Element)(d.getElementsByTagName("table").item(0));
		try 
		{
			//TODO: Whether we should encrypt the group string here?
			Element groupElement = XmlManager.getChildElement(tableElement, "Group", "id", groupID);
			Element userElement = XmlManager.getChildElement(groupElement, "User", "name", userName);
			groupElement.removeChild(userElement);
			saveGroupInfo(d);
			//add this membership to that user xml file, too
			um.deleteMmbership(userName, groupID);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	
	
	
	//test if a user is a member has the record of the membership of a group
	public boolean isMemberOf(String username, String groupID)
	{
		ArrayList<String> g = getMembers(groupID);
		if(g == null){
		  return false;
		}
		return g.contains(username);
		
	}
	
	
	
	/**
	 * get all members of a group
	 * @param   groupID	  the id of that group
	 * @return	a list of strings denotes all the members in that group
	 */
	public ArrayList<String> getMembers(String groupID)
	{
		try
		{
			if(!groupIDExists(groupID))
				return new ArrayList<String>();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		ArrayList<String> groups = new ArrayList<String>();
		
		Document d = getGroupInfo();
		//add membership
		Element tableElement = (Element)(d.getElementsByTagName("table").item(0));
		try 
		{
			//TODO: Whether we should encrypt the group string here?
			Element userElement = XmlManager.getChildElement(tableElement, "Group", "id", groupID);
			
			//String groupString = userElement.getTextContent();
			//String[] params = groupString.split(";");
			//for(int i= 0; i< params.length; i++)
			//{
			//	groups.add(params[i]);
			//}
			
			for(int i= 0; i< userElement.getElementsByTagName("User").getLength(); i++)
			{
				Element groupElement = (Element) userElement.getElementsByTagName("User").item(i);
				groups.add(groupElement.getAttribute("name"));
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
	 * find a group by groupID in xml file
	 * @param   groupID	  the id of that group
	 * @return	an Group object contains information of that group
	 */
	public Group getGroup(String groupID)
	{
		if(!groupIDExists(groupID))
			return null;
		//else
		Document d = getGroupInfo();
		Element tableElement = (Element)(d.getElementsByTagName("table").item(0));
		try
		{
			Element groupElement = XmlManager.getChildElement(tableElement, "Group", "id", groupID);
			String owner = groupElement.getAttribute("owner");
			Group group = new Group();
			group.createGroup(owner.trim());
			group.setGroupID(groupID);
			group.setRoster(getMembers(groupID));
			return group;
			
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public Date getJoinTime(String userName, String groupID)
	{
		if(isMemberOf(userName, groupID))
		{
			return null;
		}
		//else
		Document d = getGroupInfo();
		Element tableElement = (Element)(d.getElementsByTagName("table").item(0));
		try
		{
			Element groupElement = XmlManager.getChildElement(tableElement, "Group", "id", groupID);
			Element userElement = XmlManager.getChildElement(groupElement, "User", "name", userName);
			
			String dateString = userElement.getAttribute("time");
			return format.parse(dateString);  
			
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	
}

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Organizational structure for member to member communication
 *
 */
/**
 * @author sjarvie
 *
 */
public class Group {
  
  /** The owner id of the group */
  private String ownerName;
  
  /** Member List */
  private ArrayList<String> roster;
  
  /** Unique Group Identifier */
  private String groupID;
  
  /** Date format */
  private final DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

  /**
   * @return the format
   */
  public DateFormat getFormat() {
    return format;
  }

  /**
   * @param ownerName the ownerName to set
   */
  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }

  /**
   * @param roster the roster to set
   */
  public void setRoster(ArrayList<String> roster) {
    this.roster = roster;
  }

  /**
   * @param groupID the groupID to set
   */
  public void setGroupID(String groupID) {
    this.groupID = groupID;
  }

  /**
   * create a new group by input the owner ID 
   * @param: the owner ID
   * @return
   */
  public void createGroup(String ownerName){
    
    // Check whether this user is in the in database
    UserManager um = new UserManager();
    this.roster = new ArrayList<String>();
    try {
      if(!um.usernameExists(ownerName)) { 
        System.out.println("Group creating failed: No such user exists:"+ ownerName);
        return;
      }
    } 
    catch (Exception e) {
      e.printStackTrace();
    }
	
    this.ownerName = ownerName;
    
    Date curTime = new Date(System.currentTimeMillis());
    this.groupID = ownerName + format.format(curTime);
    
    GroupManager gm = new GroupManager();
    gm.createANullGroup(this.groupID, this.ownerName);
    
    this.addUser(ownerName);
  }

  /**
   * Restore the group from XML file 
   * @param: the group ID
   */
  public void RestoreGroup(String groupID){
    GroupManager gm = new GroupManager();
    this.setGroupID(groupID);
    this.setOwnerName(gm.getGroup(groupID).getOwnerName());
    this.setRoster(gm.getGroup(groupID).getRoster());
  }

  /**
   * Test whether a user is a member of this group 
   * @return   true     is a member
   *           false    is not a member
   */
  public boolean isMember(String name){
    return roster.contains(name);
  }
	
	
  /**
   * add a user to this group
   * @return whether or not the addition succeeded
   */
  public boolean addUser(String name) {
    //check whether this user is in the in database
    UserManager um = new UserManager();
    try {
      if (!um.usernameExists(name)) {
        System.out.println("Adding failed: No such user exists:"+ name);
        return false;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
	
    if (isMember(name)) {
      System.out.println(name+" is already a member of group: "+ this.groupID);
      return false;
    }
	
    //add user to the roster in memory
    if (this.roster.add(name)) {
      System.out.println("Adding " + name +" succeed");
      GroupManager gm = new GroupManager();
      gm.addMember(name, this.groupID);
      return true;
    }
    else {
      System.out.println("Addition failed");
      return false;
    }
}
	
  /**
	 * kick off a user from this group 
	 * @return   true     succeed
	 *           false    failed
	 */
  public boolean kickUser(String name) {
	  //test whether name is a member of this group
	  if(!isMember(name)) {
		  System.out.println("Kicking failed: Do not have such a user: "+ name);
		  return false;
	  }
	
	  for (int i = 0; i< roster.size(); i++) {
		  if (roster.get(i).equals(name)) {
			  roster.remove(i);
				GroupManager gm = new GroupManager();
				gm.deleteMember(name, this.groupID);
				System.out.println(name+" : removed from group");
				return true;
		  }
	  }
	  System.out.println(name+" : remove failed");
	  return false;
  }
	
	
	/**
	 * get the roster 
	 * @return an Arraylist<String> of roster
	 */
	public ArrayList<String> getRoster()
	{
		GroupManager gm = new GroupManager();
		roster = gm.getMembers(this.groupID);
		return roster;
	}
	
	/**
	 * get the owner name 
	 * @return an String of owner name
	 */
	public String getOwnerName() {
		return ownerName;
	}
	
	/**
	 * get the group id
	 * @return an String of the groupID
	 */
	public String getGroupID() {
		return groupID;
	}
	
	/**
   * removes all user memberships from a group
   */
	public void delete() {
		UserManager um = new UserManager();
		
		/** delete all the membership of user */
		for(String name: roster) {
			um.deleteMmbership(name, this.groupID);
		}
	}

}

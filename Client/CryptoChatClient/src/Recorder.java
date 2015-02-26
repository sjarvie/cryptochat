import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Recording class for storing message data
 *
 */
public class Recorder
{
	private final static DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final static long MAX_FILE_SIZE = 1024*1024;
	
	/** Synchronization Primitives  */
	private final static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final static Lock rl = rwl.readLock();
  private final static Lock wl = rwl.writeLock();
  
  
	/**
	 * Initialize a file for a group if not exists
	 * @param groupID
	 */
	public static void initialFile(String groupID){
		if(new File(groupID+".txt").exists()) {
			return;
		}
		File record = new File(groupID+".txt");
		try {
			record.createNewFile();
		} 
		catch (IOException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Add a record to respective file
	 * @param groupID
	 * @param date
	 * @param sender
	 * @param Content
	 */
	public static void Record(String groupID, Date date, String sender, String Content) {
  
	  // Format content
	  String formateLine ="[" + format.format(date) +"] "+ sender + " :\t" + Content +"\r\n" ;
	
	  // Atomic scan and write
	  wl.lock();
	  
	  // Check if records need to be deleted from file before insert to free up space
	  while (getFileSize(groupID) + (long)formateLine.length() > MAX_FILE_SIZE) {
	    removeFirstLine(groupID); 
	  }
	  
	  // Write new record to file
	  FileWriter fw;
	  try {
	    fw = new FileWriter(groupID+".txt",true);
	    fw.write(formateLine,0,formateLine.length());    
      fw.flush(); 
	  } 
	  catch (IOException e) {
	    e.printStackTrace();
	  }
	  finally {
		  wl.unlock();
	  }
	}
	
	/**
	 * Returns all records occuring after specified time
	 * @param groupID the group ID
	 * @param time the time marker
	 * @return the list of records
	 */
	public static ArrayList<String> getNextRecords(String groupID, Date time){
	  
  	ArrayList<String> records = new ArrayList<String>();
  	BufferedReader br;
  	String time_string;
  	Date r_date;
  	
	  //Atomic Operation
	  rl.lock();
	  try {
  	  //Read a Record
  		br = new BufferedReader(new FileReader(groupID+".txt"));
  		String data = br.readLine();
  		
  		//Parse a record's timestamp
  		
  		
  		//Read data records and add when appropriate
  		while( data!=null) {  
  		  time_string = data.split("]")[0].replace("[", "");
  	    r_date = format.parse(time_string);
  				
  			if(r_date.after(time)) {
  				records.add(data);
  			}
  				
  			//Continue reading
  		  data = br.readLine();
  		}
      br.close();
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
	  finally {
	    rl.unlock();
	  }
	  return records;
	}
	
	/**
	 * Returns the size of a group's record file
	 * @param groupID the id of the group
	 * @return the file size in bytes
	 */
	public static long getFileSize(String groupID) {
		if(!(new File(groupID+".txt").exists())) {
			return -1;
		}
		File record = new File(groupID+".txt");
		
		return record.length();
	}
	
	
	/**
	 * Removes a first line from a record file to free up space
	 * @param groupID the id of the file
	 */
	public static void removeFirstLine(String groupID) {  
	  
	  //Atomically open file for reading and writing
	  RandomAccessFile raf;
    wl.lock();
		try {
		  raf = new RandomAccessFile(groupID+".txt", "rw");
		  
		  //Initial write position                                             
	    long writePosition = raf.getFilePointer();                            
	    raf.readLine();
	    
	    // Shift the next lines upwards.                                      
	    long readPosition = raf.getFilePointer();                             

	    byte[] buff = new byte[1024];                                         
	    int n;                                                                
	    while (-1 != (n = raf.read(buff))) {                                  
	      raf.seek(writePosition);                                          
        raf.write(buff, 0, n);                                            
        readPosition += n;                                                
        writePosition += n;                                               
        raf.seek(readPosition);                                           
	    }                                                                     
	    raf.setLength(writePosition);                                         
	    raf.close();                  
		} 
		catch (IOException e) {
			e.printStackTrace();
		}          
		finally {
			wl.unlock();
		}
	                                            
	}         
}

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;



public class SimpleGroupTester {
	public static void main(String args[])
	{
		int choice = -1;
		while(choice != 0)
		{
			UserManager um = new UserManager();
			System.out.println("What do you want?\n\t1.Create a group for dummy user \"abc\"\n\t2.Add ShuaiLu to that group\n\t0.Exit");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			try 
			{
				choice = Integer.parseInt(br.readLine());
				if(choice == 0)
					System.out.println("Bye!");
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Please input 1 or 2.");
			}
			Group group = new Group();
			switch(choice)
			{
				case 1:
					System.out.println("Create a group");
					group.createGroup("abc");
					group.addUser("ShuaiLu");
					group.addUser("ShuaiLu");
					
					group.kickUser("ShuaiLu");
					
					ArrayList<String> g = group.getRoster();
					for(int i = 0; i<g.size(); i++)
					{
						System.out.println(g.get(i));
					}
				break;
			
				case 2:
					
				break;
				case 3:
					
				break;
				
				case 4: 
					
				break;
				case 5:
				break;
			}
		}
	}
}

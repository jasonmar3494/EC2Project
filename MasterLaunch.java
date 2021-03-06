//Jason Mar
/*
hashmap contains <key(amiID),value(InstanceObj(tasks,status))>
ArrayList will contain tasks

thread 1 launches instances based on task #/gets instances related statuses
thread 2 collects statuses
thread 3 assign tasks

make an object instead of arraylist with accessible status
launch 5 instances and check in certain amount of time to see if program finishes
tasks are arbitrary at this point
shared data structure of tasks/statuses
*/
import java.io.*;
import com.amazonaws.auth.*;
import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;
import java.util.*;
import java.net.*;
import org.apache.commons.codec.binary.Base64;


public class MasterLaunch{

	//number of instances to run in the beginning is 5, number can be changed up to 20
	public static int nodes = 5;
	
	//statusMap holds
	//key: AMI id of the instance
	//value: InstanceObj object that has two values, tasks left on instance and completion
	public static Map<String, InstanceObj> statusMap = new HashMap<String, InstanceObj>();
	//task list holds all the tasks 
	public static List<String> taskList = new ArrayList<String>();
	
	public static void main(String[] args) throws Exception{

		//instance thread and listener thread
		Thread it = new Thread(new InstanceThread());
		Thread lt = new Thread(new ListenerThread());
		
		//adds more nodes per 5 minutes of runtime (max of 20 nodes)
		Reminder tenNodes = new Reminder(600);
		Reminder fifteenNodes = new Reminder (900);
		Reminder twentyNodes = new Reminder (1200);
		it.start();
		lt.start();

	}


	/*thread that launches instances if there are less instances running than the number of nodes 			dictates 
	*/
	public static class InstanceThread extends Thread{
		public synchronized void run(){
			try{
				int instanceCurRun = statusMap.size();
				do{		
					//size of the HashMap represents the currently running instances			
					instanceCurRun = statusMap.size();
					if (instanceCurRun<nodes){
							
							//when instances currently running is less than desired nodes
							//launch more instances and get ami for key
							//launch using ant, build file
							Process process;
							try{
								process = new ProcessBuilder("ant", "-f", "/home/jasonmar/Documents/EC2/EC2Launch/build.xml").start();
								process.waitFor();
								InputStream is = process.getInputStream();
								InputStreamReader isr = new InputStreamReader(is);
								BufferedReader br = new BufferedReader(isr);
								String line;
								while((line = br.readLine()) != null){
									System.out.println(line);
								}
							}catch (Exception e){
								e.printStackTrace();
							}

							//gets the AMI id of the instance (what is the object handle?)
							String amiKey = getInstanceId();
							InstanceObj instanceData = new InstanceObj(); 
							//adds newly launched instance into the map
							statusMap.put(amiKey,instanceData);
					}
					Thread.sleep(1000);
				}while(instanceCurRun>0);
			}
			catch(Exception e){}
		}
	}

		
	/*listener thread listens to incoming requests to open a connection
		launches a mmultiserverthread for each slave that is launched
	*/
	public static class ListenerThread extends Thread{
		public synchronized void run(){
			try{
				//create server socket connection to receive statuses from slaves
				int portNumber = 9999;
				boolean listening = true;
				try{
						ServerSocket server = new ServerSocket(portNumber);
	
						//listens for contact with slaves						
						while(listening){
							new MMultiServerThread(server.accept()).start();
							Thread.sleep(1000);
						}
				}catch(IOException o){}		
			}catch(Exception e){}
		}
	}
	
	/*launches a thread per socket connection 
		the socket listens for status updates from each slave 
		the socket writes tasks to the slaves
		the socket writes status updates to the terminal
	*/
	public static class MMultiServerThread extends Thread{
		private Socket socket = null;
		private String instanceAMI = "";
		public MMultiServerThread(Socket socket){
			super("MMultiServerThread");
			this.socket = socket;	
		}
		
		public void run(){
			//GET THE AMI ID OF THE SLAVE

			try{	
					PrintWriter out = new PrintWriter(socket.getOutputStream(),true); 
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));		
						//runs if there are still tasks left over
						while(taskList.size()>0){
								//iterates through the map to get statuses					
								Set set = statusMap.entrySet();
								Iterator i = set.iterator();

								while(i.hasNext()){
										//gets the entry key
										Map.Entry curEntry = (Map.Entry) i.next();
										String instanceName = (String) curEntry.getKey();
										//finds the right entry in the map
										if(instanceName.compareTo(instanceAMI)==0){
											InstanceObj curInstance = (InstanceObj) curEntry.getValue();
								
											//reads two values in and updates the statusmap
											int tasksIn = in.read();
											int compIn = in.read();
											
											//sends both values through output socket
											System.out.println("Tasks Left: " + tasksIn);
											System.out.println("Completion: " + compIn);
							
			
											if(tasksIn<1){
												out.println(taskList.get(0));
											}
										statusMap.put(instanceAMI,new InstanceObj(tasksIn,compIn));
										}
									} 	
					}
				out.close();
				in.close();
				socket.close();
			} catch (IOException e){
				e.printStackTrace();
			}
		}
	}
}

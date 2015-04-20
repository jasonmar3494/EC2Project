//Jason Mar
/*
hashmap contains <key(amiID),value(InstanceObj(tasks,status))>
ArrayList will contain tasks

thread 1 launches instances based on task #/gets instances related statuses
thread 2 collects statuses
thread 3 assign tasks

make an object instead of arraylist with accessible status
launch set number of instances and check in certain amount of time to see if program finishes
*/
import java.io.*;
import com.amazonaws.auth.*;
import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.util.*;
import java.net.*;
import org.apache.commons.codec.binary.Base64;


public class MasterLaunch{

	//number of instances to run in the beginning is 2, number can be changed up to 20
	public static int nodes = 2;

	//set seconds to establish seconds between intervals
  public static int seconds = 120;


	//statusMap holds
	//key: IP address of the slave
	//value: InstanceObj object that has two values, 
	public static Map<String, InstanceObj> statusMap = new HashMap<String, InstanceObj>();

	//task list holds all the tasks
	//initialize tasks list through accessing Amazon S3 directories
	//each task is a filepath
	public static List<Integer> taskList = new ArrayList<Integer>();
	
	
	public static void main(String[] args) throws Exception{
		Random rand = new Random();
		for(int i=0;i<10000;i++){		
			int n = rand.nextInt(100) + 1;
			taskList.add(n);
		}
		//instance thread and listener thread
		Thread it = new Thread(new InstanceThread());
		Thread lt = new Thread(new ListenerThread());
		
		//adds more nodes per seconds of runtime (max of 20 nodes)
		Reminder tenNodes = new Reminder(seconds); 
		Reminder fifteenNodes = new Reminder (seconds*3);
		Reminder twentyNodes = new Reminder (seconds*4);
		it.start();
		lt.start();
		
	}
	
	/*thread that launches instances if there are less instances running than the number of nodes 			dictates 
	*/
	public static class InstanceThread extends Thread{
		public synchronized void run(){
			try{
				int instanceCurRun = 0;
				do{		
					//size of the HashMap represents the currently running instances			
					if (instanceCurRun<nodes){
							
							//when instances currently running is less than desired nodes
							//launch more instances and get ami for key
						
							InputStream credentialsAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("AwsCredentials.properties");
  							//Preconditions.checkNotNull(credentialsAsStream, "File 'AwsCredentials.properties' NOT found in the classpath");
		
							AWSCredentials credentials = new PropertiesCredentials(credentialsAsStream);

							AmazonEC2 ec2 = new AmazonEC2Client(credentials);
							ec2.setEndpoint("ec2.us-west-2.amazonaws.com");

							RunInstancesRequest ec2launch = new RunInstancesRequest();
							//launches the automatic slave launcher within image
							ec2launch.withImageId("ami-fe4820ce");
	
							ec2launch.withInstanceType("t1.micro");
							ec2launch.withMinCount(1);
							ec2launch.withMaxCount(1);
							ec2launch.withKeyName("keypair1");
							ec2launch.withSecurityGroups("quick-start-1");

							//sets the userscript for launching script at startup
							ec2launch.setUserData(getUserDataScript());		
		
							RunInstancesResult runInstances = ec2.runInstances(ec2launch);
							//gets the AMI id of the instance 
							String amiKey = ec2launch.getImageId();
							System.out.println("Launching Instance: (amiID) " + amiKey);
							//keeps track of how many current running instances
							instanceCurRun++;
					}
					//lets the user know when nodes are launching
					if(instanceCurRun % nodes == 0){
						System.out.println("Launching "+nodes+" more threads in "+seconds+" seconds");						
						Thread.sleep(seconds*1000);
					}
				}while(instanceCurRun<=nodes);
			}
			catch(Exception e){}
		}
	}
	
	private static String getUserDataScript(){
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("#!/bin/bash");
		lines.add("cd /home/ubuntu/MasterLaunch");
		lines.add("sh launcher.sh");
		String str = new String(Base64.encodeBase64(join(lines, "\n").getBytes()));
		return str;
	}
	static String join(Collection<String> s, String delimiter){
		StringBuilder builder = new StringBuilder();
		Iterator<String> iter = s.iterator();
		while(iter.hasNext()){
			builder.append(iter.next());
			if(!iter.hasNext()){
				break;
			}
			builder.append(delimiter);
		}
		return builder.toString();
	}

		
	/*listener thread listens to incoming requests to open a connection
		launches a mmultiserverthread for each slave that is launched
	*/
	public static class ListenerThread extends Thread{
		public static String bucketName = "serveripinfo";
		public static String keyName = "keyone";
		public static String uploadFileName = "currentip";
		public synchronized void run(){
			try{
				//create server socket connection to receive statuses from slaves
				int portNumber = 9999;
				boolean listening = true;
				try{
						//sets up an Amazon S3 connection
						InputStream credentialsAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("AwsCredentials.properties");
 						AWSCredentials credentials = new PropertiesCredentials(credentialsAsStream);
						AmazonS3 s3 = new AmazonS3Client(credentials);
						
						//sets up a server socket and gets the ip address
						ServerSocket server = new ServerSocket(portNumber, 50, InetAddress.getLocalHost());
						String serverIP = server.getInetAddress().toString();
				
						//creates a new file and writes to it the ip address of the server
						File fil = new File(uploadFileName);
						try {
							FileWriter fw = new FileWriter(fil.getAbsoluteFile());
							BufferedWriter bw = new BufferedWriter(fw);
							bw.write(serverIP);
							bw.newLine();
							bw.close();
							fw.close();
						} catch (IOException e) {}
						s3.putObject(new PutObjectRequest(bucketName, keyName, fil));		
					
						
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
		private String instanceIP = "";
		public MMultiServerThread(Socket socket){
			super("MMultiServerThread");
			this.socket = socket;	
		}
		
		public void run(){
			instanceIP = socket.getRemoteSocketAddress().toString();
			InstanceObj instanceData = new InstanceObj(); 
			//adds newly launched instance into the map
			MasterLaunch.statusMap.put(instanceIP,instanceData);
			try{	
					PrintWriter out = new PrintWriter(socket.getOutputStream(),true); 
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));		
					//creates a new file with map data
						File fil2 = new File("MapData");
						InputStream credentialsAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("AwsCredentials.properties");
 						AWSCredentials credentials = new PropertiesCredentials(credentialsAsStream);
						AmazonS3 s3 = new AmazonS3Client(credentials);
						
						//runs if there are still tasks left over
						while(MasterLaunch.taskList.size()>0){
								try {
									FileWriter fw2 = new FileWriter(fil2.getAbsoluteFile());
									BufferedWriter bw2 = new BufferedWriter(fw2);
									for(Map.Entry<String,InstanceObj> e: MasterLaunch.statusMap.entrySet()){
										String s = e.getKey();
										InstanceObj inst = e.getValue();
										String slaveid = s + ": ";
										String tasktot = inst.getTaskTotal() + ",";
										String comptot = inst.getCompTotal() + "";
										String infowrite = slaveid+tasktot+comptot;
										bw2.write(infowrite);
										bw2.newLine();
									}
									bw2.newLine();
									bw2.close();
									fw2.close();
								} catch (IOException e) {}
								s3.putObject(new PutObjectRequest("serveripinfo", "MapData", fil2));								
								//iterates through the map to get statuses					
								Set set = MasterLaunch.statusMap.entrySet();
								Iterator i = set.iterator();
								while(i.hasNext()){
										//gets the entry key
										Map.Entry curEntry = (Map.Entry) i.next();
										String instanceName = (String) curEntry.getKey();
										//finds the right entry in the map
										if(instanceName.compareTo(instanceIP)==0){
											InstanceObj curInstance = (InstanceObj) curEntry.getValue();
								
											//reads two values in and updates the statusmap
											//"TotalTasks,CompStatus,TaskID,Tasks"
											String tasksIn = in.read() + "";
											String[] result = tasksIn.split(",");
	
											//updates values inside instance											
											curInstance.setTaskTotal(Integer.parseInt(result[0]));
											curInstance.setCompTotal(Integer.parseInt(result[1]));
											curInstance.setTaskStatus(result[2], Integer.parseInt(result[3]));
											
											System.out.println("Slave IP: " + instanceIP);
											System.out.println("Total Tasks: " + result[0]);
											System.out.println("Completion: " + result[1]);
											System.out.println("Task Number: " + result[2]);
											System.out.println("Tasks: " + result[3]);
													
											
											//get the first task in the task list and sends it through output socket if 
											if((Integer.parseInt(result[3]))<1){
												out.println(MasterLaunch.taskList.get(0));
												MasterLaunch.taskList.remove(0);
											}
										//put the updated status in the status Map
										MasterLaunch.statusMap.put(instanceIP,curInstance);
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

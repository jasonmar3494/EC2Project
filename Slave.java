import java.io.*;
import com.amazonaws.auth.*;
import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import java.util.*;
import java.net.*;
import org.apache.commons.codec.binary.Base64;

public class Slave {
	
	static Queue<Job> JobsToRun = new LinkedList<Job>();
	static Queue<Job> RunningJobs = new LinkedList<Job>();
	static Queue<Job> FinishedJobs = new LinkedList<Job>();
	

	public static void main(String args[]){
		Thread ta = new Thread(new TaskAdmission());
		Thread launcher = new Thread(new Launcher());
		Thread sr = new Thread(new StatusReport());
		ta.start();
		launcher.start();
		sr.start();
	}

	// method to enter jobs into the queue
	public synchronized static void updateEntry(Job j1) {	
		JobsToRun.add(j1);
		System.out.println("Hello from entry adder in jobs to run!");
	}
	
	public static class TaskAdmission extends Thread {
		public synchronized void run() { 							
			try {
				String bucketName = "serveripinfo";
				String keyName = "keyone";
				String uploadFileName = "currentip";
				InputStream credentialsAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("AwsCredentials.properties");
				
				//credentials for setting up AWS connection
 				AWSCredentials credentials = new PropertiesCredentials(credentialsAsStream);
				AmazonS3 s3 = new AmazonS3Client(credentials);
				//gets the s3 object containing the IP address
				S3Object object = s3.getObject(new GetObjectRequest(bucketName, keyName));
				InputStream objectData = object.getObjectContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(objectData));
				StringBuilder out = new StringBuilder();
				String line;
				while((line = reader.readLine()) != null){
					out.append(line);
				}
				//MasterIP stored in variable masterIP
				String masterIP = out.toString();
				System.out.println(out.toString());
				reader.close();

				// create new socket connection between master and slave
				int portNumber = 9999;
				Socket s = new Socket(masterIP, portNumber); 	

				// buffered reader to read input from master
				BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));	
				PrintWriter out = new PrintWriter(s.getOutputStream(),true);
				while (true) {
					Thread.sleep(2000);
					String je = br.read();	// put the next value from buffered reader into job
					int job = Integer.parseInt(je);
					System.out.println("The value read is: " + job + ".");
					if (job != 0) {
						System.out.println("The job received from master is: " + job + ".");
						Job j = new Job(job,"new");
						updateEntry(j);
						int j1 = JobsToRun.size();
						int j2 = RunningJobs.size();
						int j3 = FinishedJobs.size();
						int tot = j1+j3;
						double jc = (j3/tot) *100;
						int jcs = (int) jc;
						String j = ""+tot+","+jc+",1,1";
						
						
					}
				}
			} catch (Exception e) {
				
			}
		}
	}

	// launcher thread that checks queue and starts executor threads
	public static class Launcher extends Thread {	
		public synchronized void run() {						
			try {
				while (true) {
					Thread.sleep(500);
					if (JobsToRun.size() > 0) { 		// there is at least 1 job that needs to run!
						new Executor().start();				// create new executor thread to do the job
					} else {
						continue;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// thread that actually performs the action needed on the job
	public static class Executor extends Thread {	
		int taskId;
		String curState;
		public synchronized void run() {
			try {
				Job newjob = JobsToRun.poll();		// gets the first value off of the new jobs queue
				this.taskId = newjob.taskId;			// sets local values equal to the values of that job
				this.curState = newjob.curState;
				
				newjob.curState = "running";
				RunningJobs.add(newjob);				// since job is now running, add it to the runningjobs queue
				
				for (int i = 0; i < 10000; i++) {	
					taskId += i;									// perform task
				}
				System.out.println("The new task value after computation is: " + taskId + ".");
				
				newjob.curState = "finished";
				FinishedJobs.add(newjob);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static class StatusReport extends Thread {
		int j1, j2, j3;
		
		public synchronized void run() { 			
			try {
				while (true) {
					Thread.sleep(5000);
					
					j1 = JobsToRun.size();
					System.out.println("There are " + j1 + " jobs on the queue, waiting to run.");
					j2 = RunningJobs.size();
					System.out.println("There are " + j2 + " jobs running at the moment!");
					j3 = FinishedJobs.size();
					System.out.println("There are " + j3 + " jobs that have finished.");
				}
			} catch (Exception e) {
				
			}
		}
	}

	
}

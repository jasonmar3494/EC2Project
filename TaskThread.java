import java.util.*;

public class TaskThread implements Runnable{
	
	Thread t;
	public TaskThread(){
		t = new Thread(this, "TaskThread");
		t.start();
	}
		
	public void run(){
		try{
				
			//JAVA SOCKET
			//take # nodes from information and divide data into equal number of nodes
			//puts the separated tasks into an Arraylist accessible by all threads
			Thread.sleep(10);

		}
		catch(InterruptedException e){}
	}
}

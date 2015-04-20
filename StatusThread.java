import java.util.*;
import java.io.*;

public class StatusThread extends Thread{
	public synchronized void run(){
		try{
			while(MasterLaunch.taskList.size()>0){
				//runs through the map to get statuses					
				Set set = MasterLaunch.statusMap.entrySet();
				Iterator i = set.iterator();
				while(i.hasNext()){
					Map.Entry curEntry = (Map.Entry) i.next();
					String instanceName = curEntry.getKey();
					InstanceObj curInstance = curEntry.getValue();
					int runStatus = curInstance.getStatusT();
					int comStatus = curInstance.getStatusC();
					//either print or send through socket the instance Id and statuses

					//inserting new statuses ----received status from socket
					//statusMap.put(//INSTANCEID,new InstanceObj(//,//));
				} 	
				//get statuses from slaves with socket (implement later)
				Thread.sleep(10);
			}		
		}
		catch(Exception e){}
	}
}

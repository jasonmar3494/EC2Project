import java.io.*;
import java.util.*;

public class InstanceObj{
	
	//METHODS
	//public InstanceObj(int totalTasks, int totalCompletion) --constructor
	//public int getTaskTotal() --gets number of total tasks on slave
	//public int getCompTotal() --gets completion status of slave
	//public void setTaskTotal(int runningTasks) --sets tasks total for slave
	//public void setCompTotal(int completion) --sets completion for slave
	//public int getTaskStatus(String taskNumber) --gets the # of tasks running on a thread in the 														slave
	//public void setTaskStatus(String taskNumber, int newTaskStatus) --sets the updated # of tasks in 																			a Thread on the slave  
	//public void createNewTaskNumber(int newTaskStatus) --creates a new entry for a new Thread in the 																slave
	//public int getHashMapSize() --gets the size of the hashmap

	//second layer of status Map to keep track of individual threads and their tasks in each slave
	private Map<String, Integer> iStatusMap = new HashMap<String, Integer>();
	private int numberOfTasks;
	private int statusOfCompletion;
	
	//default constructor
	public InstanceObj(){
		numberOfTasks = -1;
		statusOfCompletion = -1;
	}

	//constructor
	public InstanceObj(int totalTasks, int totalCompletion){
		numberOfTasks = totalTasks;
		statusOfCompletion = totalCompletion;
	}
	
	//returns number of tasks running total on slave. If tasks are 0, then it is not running
	public int getTaskTotal(){
		return numberOfTasks;
	}

	//returns X/100 for percentage of task completion for slave instance as a whole
	public int getCompTotal(){
		return statusOfCompletion;
	}
			
	//sets the status to run or not running for instance
	public void setTaskTotal(int runningTasks){
		numberOfTasks = runningTasks;
	}	

	//set from 1-100 status of instance
	public void setCompTotal(int completion){
		statusOfCompletion = completion;
	}

	//gets the number of tasks running on a thread within the slave
	public int getTaskStatus(String taskNumber){
		int taskStatus = (int) iStatusMap.get(taskNumber);
		return taskStatus;
	}

	//sets the number of tasks running on a thread within the slave
	public void setTaskStatus(String taskNumber, int newTaskStatus){
		iStatusMap.put(taskNumber, newTaskStatus);
	}
	
	//creates a new entry for a Thread of tasks in the slave
	public void createNewTaskNumber(int newTaskStatus){
		int size = iStatusMap.size();
		int tN = size+1;
		String taskNo = Integer.toString(tN);
		iStatusMap.put(taskNo, newTaskStatus);
	}

	//gets the size of the HashMap 
	public int getHashMapsize(){
		return iStatusMap.size();
	}
}

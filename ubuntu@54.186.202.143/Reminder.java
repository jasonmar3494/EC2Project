import java.util.*;

public class Reminder{
	Timer instanceTimer;

	//SET THIS VALUE TO CHANGE HOW MANY INSTANCES LAUNCH PER TIME INTERVAL//
	private int nodeIncrement = 2;


	public Reminder(int seconds){
		instanceTimer = new Timer();
		instanceTimer.schedule(new RemindTask(), seconds*1000);
	}
	
	class RemindTask extends TimerTask{
		public void run(){
			MasterLaunch.nodes = MasterLaunch.nodes + nodeIncrement;
			System.out.println("Launching " + nodeIncrement + " more instances");
			instanceTimer.cancel();		
		}
	}
	
}


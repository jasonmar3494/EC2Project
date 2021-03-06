import java.util.*;

public static class InstanceThread extends Thread{
	public synchronized void run(){
		try{
			do{		
				//size of the HashMap represents the currently running instances			
				int instanceCurRun = MasterLaunch.statusMap.size();
				if (instanceCurRun<MasterLaunch.nodes){
					/*
						when instances currently running is less than desired nodes
						launch more instances and get ami for key */
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
						String amiKey = getImageId();
						InstanceObj instanceData = new InstanceObj(); 
						MasterLaunch.statusMap.put(amiKey,instanceData);
				}

				Set set = MasterLaunch.statusMap.entrySet();
				Iterator i = set.iterator();
				while(i.hasNext()){
					Map.Entry curEntry = (Map.Entry) i.next();
					InstanceObj curInstance = curEntry.getValue();					
					if(curInstance.getStatusT()<1){
						//ASSIGN TASK THROUGH JAVA SOCKET?
					}
				}
				Thread.sleep(10);
			}while(instanceCurRun>0)
		}
		catch(Exception e){}
	}
}

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


public class testBucket{
	public static String bucketName = "serveripinfo";
	public static String keyName = "keyone";
	public static String uploadFileName = "currentip";
	public static List<Integer> taskList1 = new ArrayList<Integer>();
				public static List<Integer> taskList2 = new ArrayList<Integer>();
				public static List<Double> taskList3 = new ArrayList<Double>();
				public static List<Integer> taskList4 = new ArrayList<Integer>();
	public static void main(String[] args) throws Exception{
				
				int testsize = 1000000;
				Random rand = new Random();
				for(int i=0;i<testsize;i++){		
						int n = rand.nextInt(10000) + 1;
						taskList1.add(n);
				}	
				Random rand2 = new Random();
				for(int j=0;j<testsize;j++){		
						int m = rand2.nextInt(10000) + 1;
						taskList2.add(m);
				}	
				Random rand3 = new Random();
				for(int j=0;j<testsize;j++){		
						int m = rand3.nextInt(10000) + 1;
						taskList4.add(m);
				}	
				long startTime = System.nanoTime();
				for(int k=0;k<testsize;k++){		
						double o = Math.tan(taskList1.get(k) * taskList2.get(k) * taskList4.get(k));
						taskList3.add(o);
				}
				long endTime = System.nanoTime();
				long duration = endTime - startTime;
				String d = String.valueOf(duration);		
						InputStream credentialsAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("AwsCredentials.properties");
 						AWSCredentials credentials = new PropertiesCredentials(credentialsAsStream);
	
						AmazonS3 s3 = new AmazonS3Client(credentials);
						File fil = new File(uploadFileName);
						try {
							FileWriter fw = new FileWriter(fil.getAbsoluteFile());
							BufferedWriter bw = new BufferedWriter(fw);
							bw.write(d);
							bw.newLine();
							bw.close();
							fw.close();
						} catch (IOException e) {}
						s3.putObject(new PutObjectRequest(bucketName, keyName, fil));
			
		S3Object object = s3.getObject(new GetObjectRequest(bucketName, keyName));
			InputStream objectData = object.getObjectContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(objectData));
			StringBuilder out = new StringBuilder();
			String line;
			while((line = reader.readLine()) != null){
				out.append(line);
			}
			String changed = out.toString();
			System.out.println(changed);
			/*int index = 0;
			for(int i=0;i<changed.length();i++){
				String e = changed.charAt(i) + "";
				if(e.compareTo("/") == 0)
					index = i;
				}	
			
			String substringIP = changed.substring(index+1, changed.length());
			System.out.println(substringIP);
			*/
			reader.close();

			
	}
}


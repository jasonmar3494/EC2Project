import java.io.*;
import com.amazonaws.auth.*;
import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;
import java.util.*;
import org.apache.commons.codec.binary.Base64;

public class Ec2Launch{
	public static void main(String[] args)throws Exception{
		
		InputStream credentialsAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("AwsCredentials.properties");
  		//Preconditions.checkNotNull(credentialsAsStream, "File 'AwsCredentials.properties' NOT found in the classpath");
		
		AWSCredentials credentials = new PropertiesCredentials(credentialsAsStream);

		AmazonEC2 ec2 = new AmazonEC2Client(credentials);
		ec2.setEndpoint("ec2.us-west-2.amazonaws.com");

		RunInstancesRequest ec2launch = new RunInstancesRequest();
		//launches the automatic slave launcher within image
		ec2launch.withImageId("ami-c24820f2");
	
		ec2launch.withInstanceType("t1.micro");
		ec2launch.withMinCount(1);
		ec2launch.withMaxCount(1);
		ec2launch.withKeyName("keypair1");
		ec2launch.withSecurityGroups("quick-start-1");
		ec2launch.setUserData(getUserDataScript());		
		
		RunInstancesResult runInstances = ec2.runInstances(ec2launch);

		
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
}


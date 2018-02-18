package ml224ec_assign2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class AccessPolicy {
	
	private static final Map<String, Boolean> accessPolicies = 
			new HashMap<String, Boolean>();
	
	private static boolean defaultPolicy = true;

	public static void initialize()
	{
		try {
			byte[] policyData = Files.readAllBytes(
					Paths.get(WebServer.CONTENT_PATH + '/' + "access_policy.config"));
			
			String dataString = new String(policyData);
		} catch (IOException e) {
			System.out.println("Access policy file \"access_policy.config\" not found, all-access is active");
		}
	}
	
	public static boolean accessAllowed(String path)
	{
		Boolean bool = accessPolicies.get(path);
		if (bool != null)
			return bool.booleanValue();
		return defaultPolicy;
	}
}

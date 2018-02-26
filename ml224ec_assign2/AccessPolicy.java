package ml224ec_assign2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * A rudiamentary and simple, yet essentially functional implementation of access policy
 * regulating access to locations in the web server's content folder.
 * @author Martin Lyrå
 *
 */
public class AccessPolicy {
	
	private static final String POLICY_CONFIG_NAME = "access_policy.config";
	
	private static final Map<String, Boolean> accessPolicies = 
			new HashMap<String, Boolean>();
	
	private static boolean defaultPolicy = true;

	/**
	 * Look for and load access policy if there exists one in the serving content folder
	 */
	public static void initialize()
	{
		/* it is always a good idea to restrict access to the policy configuration file */
		accessPolicies.put(POLICY_CONFIG_NAME, false);
		
		try {
			Path path = Paths.get(WebServer.CONTENT_PATH + '/' + POLICY_CONFIG_NAME);
			byte[] policyData = Files.readAllBytes(path);
			
			String dataString = new String(policyData);
			
			boolean setPolicy = false;
			for (String line : dataString.split("\n"))
			{
				/* Filter out commenting (traditional line comments using '#') */
				int commentIndex = line.indexOf("#");
				if (commentIndex > -1)
					line = line.substring(0, commentIndex);
				
				/* Filter out whitespaceing & ignore empty lines */
				line = line.trim();
				if (line.isEmpty())
					continue;
				
				line = line.replace("/", "\\"); // replace backs-lashes with Windows' forward-slashes
				
				/* Command */
				if(line.startsWith("$"))
				{	
					if (line.contains("allow"))
					{
						if (line.endsWith("all"))
							defaultPolicy = true;
						else
							setPolicy = true; // set any entires after this as allowed
					}
					else if (line.contains("forbid"))
					{
						if (line.endsWith("all"))
							defaultPolicy = false;
						else
							setPolicy = false; // set any entries after this as forbidden
					}
					else if (line.contains("end")) // reset to default policy
						setPolicy = defaultPolicy;
					
					continue;
				}
				
				accessPolicies.put(line, setPolicy);
			}
		} catch (IOException e) {
			System.out.println("Access policy file \"access_policy.config\" not found, all-access is active");
		}
	}
	
	/**
	 * Check whether the access regulation rules out access to path. 
	 * Done by searching for a "contains()"-match between key and path.
	 * @param path
	 * @return true if access is allowed, false if access is forbidden
	 */
	public static boolean accessAllowed(String path)
	{
		for (String key : accessPolicies.keySet())
		{
			if (path.contains(key))
			{
				if (WebServer.DEBUG)
					System.out.printf("Key match for %s with %s\n", path, key);
				Boolean policy = accessPolicies.get(key);
				if (policy != null)
					return policy.booleanValue();
			}
		}
		if (WebServer.DEBUG)
			System.out.printf("No key match for %s, using following omni-policy: %s\n", path,
					defaultPolicy ? "allow all" : "forbid all");
		return defaultPolicy;
	}
}

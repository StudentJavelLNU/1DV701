package ml224ec_assign2;

import java.util.HashMap;
import java.util.Map;

public class HttpParser {
	
	private static final String CRLF = "\r\n";

	public static Map<String, String> parse(String messageString)
	{
		Map<String, String> fields = new HashMap<String, String>();
		
		String[] parts = messageString.split(CRLF);
		
		fields.put("Header-Top", parts[0]);
		
		boolean endOfHeader = false;
		String contentDataString = "";
		for (int i = 1; i < parts.length; i++)
		{
			String part = parts[i];
			if (!endOfHeader)
			{
				if (part.isEmpty())
					endOfHeader = true;
				else
				{
					int si = part.indexOf(':');
					if (si > -1)
						fields.put(part.substring(0, si), part.substring(si+1).trim());
				}
			}
			else
				contentDataString += part + CRLF;
		}
		
		fields.put("Content-Data", contentDataString);
		
		return fields;
	}
	
	public static String getAttribute(String targetAttribute, String sourceString)
	{	
		for (String param : sourceString.split("; "))
		{
			if (param.contains(targetAttribute+'='))
				return param.split("=")[1];
		}
		return null;
	}
}

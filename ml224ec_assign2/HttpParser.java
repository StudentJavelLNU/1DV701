package ml224ec_assign2;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class HttpParser {
	
	private static final String CRLF = "\r\n";
	
	private static final Map<String, String> MEDIA_TYPES = 
			new HashMap<String, String>()
	{
		/**
		 * Eclipse won't fucking shut up telling me about adding a serial number.
		 */
		private static final long serialVersionUID = 1L;

		{
			put("htm", "text/html");
			put("html", "text/html");
			put("txt", "text/plain");
			put("xml", "text/xml");
			put("css", "text/css");
			put("png", "image/png");
			put("gif", "image/gif");
			put("jpg", "image/jpg");
			put("jpeg", "image/jpeg");
		}
	};

	public static Map<String, String> parse(String messageString, boolean toplessHeader)
	{
		Map<String, String> fields = new HashMap<String, String>();
		int startOfHeader = -1;
		
		String[] parts = messageString.split(CRLF);
		
		/* some browsers add extra CRLF breaks before the header, skip them */
		for (int i = 0; i < parts.length; i++)
		{
			String part = parts[i];
			if (part.isEmpty())
				continue;
			else
			{
				startOfHeader = i;
				break;
			}
		}
			
		if (!toplessHeader)
		{
			fields.put("Header-Top", parts[startOfHeader++]);
		}

		boolean endOfHeader = false;
		String contentDataString = "";
		for (int i = startOfHeader; i < parts.length; i++)
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
	
	public static String getArbitaryString(byte[] arbitaryData)
	{
		try {
			return new String(arbitaryData, "ISO-8859-15");
		} catch (UnsupportedEncodingException e) {
			return new String(arbitaryData);
		} catch (NullPointerException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public static byte[] getArbitaryData(String arbitaryString)
	{
		try {
			return arbitaryString.getBytes("ISO-8859-15");
		} catch (UnsupportedEncodingException e) {
			return arbitaryString.getBytes();
		} catch (NullPointerException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}
	
	public static String getContentType(String fileExtension)
	{
		return MEDIA_TYPES.get(fileExtension);
	}
}

package ml224ec_assign2;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class HttpParser {
	
	/**
	 * String for Carriage-Return (\r} and Line-Feed-Break (\n) at once.
	 */
	private static final String CRLF = "\r\n";
	
	/**
	 * A map that serves as a dictionary for content types when forming a HTTP reply
	 */
	public static final Map<String, String> MEDIA_TYPES = 
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

	/**
	 * Parses HTTP header and body into a single (Hash)Map of keys with associated values.
	 * @param messageString - the HTTP head and body as a single string
	 * @param toplessHeader - True for the parser to treat the first header line as if it is a field
	 * (e.g. the input HTTP is not a request or response)
	 * @return
	 */
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
		
		/* If the Header has a top (is a Request or Response), put it as a special entry */
		if (!toplessHeader)
		{
			fields.put("Header-Top", parts[startOfHeader++]);
		}

		/* Parse all data, header first, then body */
		boolean endOfHeader = false;
		String contentDataString = "";
		for (int i = startOfHeader; i < parts.length; i++)
		{
			String part = parts[i];
			
			/* Head */
			if (!endOfHeader)
			{
				/* An empty line with only CRLF is thought to be an divider between header and body */
				if (part.isEmpty())
					endOfHeader = true;
				else
				{
					int si = part.indexOf(':');
					if (si > -1)
						fields.put(part.substring(0, si), part.substring(si+1).trim());
				}
			}
			/* Body */
			else
				contentDataString += part + CRLF;
		}
		
		fields.put("Content-Data", contentDataString);
		
		return fields;
	}
	
	/**
	 * Get an attribute from input string if there is any. Null if none was found.
	 * @param targetAttribute
	 * @param sourceString
	 * @return
	 */
	public static String getAttribute(String targetAttribute, String sourceString)
	{	
		for (String param : sourceString.split("; "))
		{
			if (param.contains(targetAttribute+'='))
				return param.split("=")[1];
		}
		return null;
	}
	
	/**
	 * Helper function for converting an arbitary byte array to a string without data loss due to
	 * encoding. By encoding the string with 8-bit charset.
	 * @param arbitaryData
	 * @return
	 */
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
	
	/**
	 * Helper function for converting an arbitary string (a string encoded with 8-bit charset) back
	 * to an arbitary byte array.
	 * @param arbitaryString
	 * @return
	 */
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
	
	/**
	 * Returns the media content type associated with given file extension.
	 * @param fileExtension
	 * @return
	 */
	public static String getContentType(String fileExtension)
	{
		return MEDIA_TYPES.get(fileExtension);
	}
}

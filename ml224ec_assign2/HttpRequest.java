package ml224ec_assign2;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequest {
	
	private Map<String, String> fields = 
			new HashMap<String, String>();
	private final List<HttpContent> attachedContent =
			new ArrayList<HttpContent>();
	
	private final String originalRequestString;
	
	HttpRequest(String httpRequestString)
	{
		originalRequestString = httpRequestString;
		parseRequestString(httpRequestString);
	}
	
	private void parseRequestString(String message)
	{
		fields = HttpParser.parse(message);
		
		String[] request = fields.get("Header-Top").split(" ");
		
		fields.put("Method", request[0]);
		fields.put("RequestLocation", request[1]);
		fields.put("HttpStandard", request[2]);
		
		if (request[0].equals("POST"))
			parseContentSection();
	}
	
	private void parseContentSection()
	{
		if (fields.containsKey("Content-Data"))
		{
			String contentInfo = fields.get("Content-Type");
			if (contentInfo.contains("boundary="))
			{
				String boundaryName = HttpParser.getAttribute("boundary", contentInfo);
				
				String[] dataParts = fields.get("Content-Data").split("--"+boundaryName);
				for(String part : dataParts)
				{
					if (part.isEmpty() || part.equals("--\r\n"))
						continue;
					try {
						attachedContent.add(new HttpContent(part));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public List<HttpContent> getParsedContent()
	{
		return attachedContent;
	}
	
	public boolean hasField(String fieldName)
	{
		return fields.get(fieldName) != null;
	}
	
	public String getMethod()
	{
		return fields.get("Method");
	}
	
	public String getRequestLocation()
	{
		return fields.get("RequestLocation");
	}
	
	public String getHttpStandard()
	{
		return fields.get("HttpStandard");
	}
	
	public String getField(String fieldName)
	{
		return fields.get(fieldName);
	}
	
	public String toString()
	{
		return originalRequestString;
	}
}

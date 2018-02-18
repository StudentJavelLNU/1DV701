package ml224ec_assign2;

import java.util.ArrayList;
import java.util.List;

public class HttpRequest extends HttpBase {
	
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
		fields = HttpParser.parse(message, false);
		
		String[] request = fields.get("Header-Top").split(" ");
		
		fields.put("Method", request[0]);
		fields.put("RequestLocation", request[1]);
		fields.put("HttpStandard", request[2]);
		
		if (request[0].equals("POST"))
		{
			parseContentSection();
			specialMethodCheck();
		}
	}
	
	private void specialMethodCheck()
	{
		HttpContent methodData = getContentByName("_method");
		if (methodData != null)
			fields.put("Method", methodData.getField("Content-Data").trim());
	}
	
	private void parseContentSection()
	{
		if (fields.containsKey("Content-Data"))
		{
			String contentInfo = fields.get("Content-Type");
			if (contentInfo.contains("boundary="))
			{
				String boundaryName = HttpParser.getAttribute("boundary", contentInfo);
				
				String contentBlock = "";
				String[] dataParts = fields.get("Content-Data").split(CRLF);
				for(String part : dataParts)
				{
					try {
						if (part.equals("--" + boundaryName) 		 // block start/denominator
						 || part.equals("--" + boundaryName + "--")) // end
						{
							if (!contentBlock.isEmpty())
								attachedContent.add(new HttpContent(contentBlock));
							contentBlock = "";
						}
						contentBlock += part + CRLF;
					} catch (Exception e) {e.printStackTrace();}
				}
			}
		}
	}
	
	public HttpContent getContentByName(String formName)
	{
		for (HttpContent c : attachedContent)
		{
			String info = c.getDisposition();
			String name = HttpParser.getAttribute("name", info).replaceAll("\"", "");
			
			if (name.equals(formName))
				return c;
		}
		return null;
	}
	
	public List<HttpContent> getParsedContent()
	{
		return attachedContent;
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
	
	public String toString()
	{
		return originalRequestString;
	}
}

package ml224ec_assign2;

import java.util.ArrayList;
import java.util.List;

/**
 * An object designed to represent a HTTP response, make its data accessible to the web server.
 * @author Martin Lyrå
 *
 */
public class HttpRequest extends HttpBase {
	
	/**
	 * List of other accessory bodies attached to this response's body.
	 */
	private final List<HttpContent> attachedContent =
			new ArrayList<HttpContent>();
	
	private final String originalRequestString;
	
	HttpRequest(String httpRequestString)
	{
		originalRequestString = httpRequestString;
		parseRequestString(httpRequestString);
	}
	
	/**
	 * Special function for parsing the top line of the request's HTTP header.
	 * @param message
	 */
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
	
	/**
	 * HTML does not support PUT method or other HTTP methods. Check the attached body
	 * for an attribute that specifies an alternative override method.
	 */
	private void specialMethodCheck()
	{
		HttpContent methodData = getContentByName("_method");
		if (methodData != null)
			fields.put("Method", methodData.getField("Content-Data").trim());
	}
	
	/**
	 * Parse the body section of the HTTP request.
	 */
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
	
	/**
	 * Returns the attached content data that has the given data (as set by the HTML form)
	 * @param formName
	 * @return
	 */
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
	
	/**
	 * Returns the list of contents that was attached to this response
	 * @return
	 */
	public List<HttpContent> getParsedContent()
	{
		return attachedContent;
	}
	
	/**
	 * Get specified method
	 * @return
	 */
	public String getMethod()
	{
		return fields.get("Method");
	}
	
	/**
	 * Get specified location requested by client
	 * @return
	 */
	public String getRequestLocation()
	{
		return fields.get("RequestLocation");
	}
	
	/**
	 * Get requested HTTP standard
	 * @return
	 */
	public String getHttpStandard()
	{
		return fields.get("HttpStandard");
	}
	
	/**
	 * Return the request in string form
	 */
	public String toString()
	{
		return originalRequestString;
	}
}

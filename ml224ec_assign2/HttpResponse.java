package ml224ec_assign2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse extends HttpBase {
	
	private static final Map<Integer, String> RESPONSE_CODES = 
			new HashMap<Integer, String>()
	{
		/**
		 * Eclipse won't fucking shut up telling me about adding a serial number.
		 */
		private static final long serialVersionUID = 1L;

		{
			put(200, "OK");
			
			put(302, "Found");
			
			put(403, "Forbidden");
			put(404, "Not Found");
			
			put(500, "Internal Server Error");
			put(502, "Method not Supported");
		}
	};
	
	private int statusCode;
	private String reason;

	HttpResponse(int statusCode)
	{
		this.statusCode = statusCode;
		
		postConstructor();
	}
	
	HttpResponse(int statusCode, String reason)
	{
		this.statusCode = statusCode;
		this.reason = reason;
		
		postConstructor();
	}
	
	private void postConstructor()
	{
		setField("Server", "ml224ec's Simple Web Server");
		
		if (isAnError())
			setContent(generateErrorPage(), "text/html");
	}
	
	public boolean isAnError()
	{
		return statusCode >= 400;
	}
	
	public void setRedirectLocation(String path)
	{
		setField("Location", path);
	}
	
	public void setContent(String content, String contentType)
	{
		setContent(content.getBytes(), contentType);
	}
	
	public void setContent(byte[] content, String contentType)
	{
		setField("Content-Data", HttpParser.getArbitaryString(content));
		setField("Content-Length", content.length + "");
		setField("Content-Type", contentType);
	}
	
	private String getReason()
	{
		if (reason != null)
			return reason;
		return RESPONSE_CODES.get(statusCode);
	}
	
	private String buildHeader()
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append(String.format("HTTP/1.1 %d %s"+CRLF, statusCode, getReason()));
		builder.append(getFieldString("Server") + CRLF);
		if (statusCode == 302)
			builder.append(getFieldString("Location") + CRLF);
		else if (statusCode == 200 || (isAnError() && hasField("Content-Data")))
		{
			builder.append(getFieldString("Content-Length") + CRLF);
			builder.append(getFieldString("Content-Type") + CRLF);
		}
		builder.append(CRLF);
		
		return builder.toString();
	}
	
	public String generateErrorPage() {
		String result = "";
		try {
			Path path = Paths.get(WebServer.TEMPLATE_PATH + "/error-template.html");
			byte[] templateContent = Files.readAllBytes(path);
			
			String template = new String(templateContent);
			
			template = template.replaceAll("%status-code%", statusCode + "");
			template = template.replaceAll("%status-reason%", getReason());
			
			result = template;
		} catch( Exception e ) { e.printStackTrace(); }
		return result;
	}
	
	public byte[] getBytes()
	{
		String header = buildHeader();
		int headLength = header.length();
		
		byte[] content = new byte[0];
		if (hasField("Content-Data"))
			content = HttpParser.getArbitaryData(getField("Content-Data"));
		
		byte[] bytes = new byte[headLength + content.length];
		System.arraycopy(header.getBytes(), 0, bytes, 0, headLength);
		System.arraycopy(content, 0, bytes, headLength, content.length);
		
		return bytes;
	}
	
	public String toString()
	{
		return buildHeader() + getField("Content-Data");
	}
}

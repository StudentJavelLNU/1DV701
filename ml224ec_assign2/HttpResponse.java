package ml224ec_assign2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HttpResponse extends HttpBase {
	
	private HttpStatusCode httpStatusCode;
	
	String reason;

	public HttpResponse(int integerCode)
	{
		this(HttpStatusCode.valueOf(integerCode));
	}
	
	public HttpResponse(HttpStatusCode code)
	{
		httpStatusCode = code;
		
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
		return httpStatusCode.getCode() >= 400;
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
		return httpStatusCode.getMessage();
	}
	
	private String buildHeader()
	{
		StringBuilder builder = new StringBuilder();
		
		int statusCode = httpStatusCode.getCode();
		
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
			
			template = template.replaceAll("%status-code%", httpStatusCode.getCode() + "");
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
	
	public HttpStatusCode getCode()
	{
		return httpStatusCode;
	}
	
	public String toString()
	{
		return buildHeader() + getField("Content-Data");
	}
}

package ml224ec_assign2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * An object designed to represent as a HTTP response. Used by this server to respond to HTTP requests.
 * @author Martin Lyrå
 *
 */
public class HttpResponse extends HttpBase {
	
	private HttpStatusCode httpStatusCode;
	
	String reason;

	/**
	 * Build a bare-bones response with a specifed integer as HTTP status code.
	 * @param integerCode
	 */
	public HttpResponse(int integerCode)
	{
		this(HttpStatusCode.valueOf(integerCode));
	}
	
	/**
	 * Build a bare-bones response with a specifed HTTP status code.
	 * @param code
	 */
	public HttpResponse(HttpStatusCode code)
	{
		httpStatusCode = code;
		
		postConstructor();
	}
	
	/**
	 * A function meant as finalization of object no matter which constructor is being used.
	 */
	private void postConstructor()
	{
		setField("Server", "ml224ec's Simple Web Server");
		
		if (isAnError())
			setContent(generateErrorPage(), "text/html");
	}
	
	/**
	 * Return whether the response's status code of 4XX (client error) or 5XX (server error).
	 * Technically, returns true for any status code that equals or is greater than 400. 
	 * @return
	 */
	public boolean isAnError()
	{
		return httpStatusCode.getCode() >= 400;
	}
	
	/**
	 * For use of 3XX status codes.
	 * @param path
	 */
	public void setRedirectLocation(String path)
	{
		setField("Location", path);
	}
	
	/**
	 * Embedds raw data content to HTTP response for client to receive.
	 * @param content
	 * @param contentType
	 */
	public void setContent(String content, String contentType)
	{
		setContent(content.getBytes(), contentType);
	}
	
	/**
	 * Embedds raw data content to HTTP response for client to receive.
	 * @param content
	 * @param contentType
	 */
	public void setContent(byte[] content, String contentType)
	{
		setField("Content-Data", HttpParser.getArbitraryString(content));
		setField("Content-Length", content.length + "");
		setField("Content-Type", contentType);
	}
	
	/**
	 * Returns a reason for the specified status code. 
	 * If one does not exist the message associated with the code is returned instead.
	 * @return
	 */
	private String getReason()
	{
		if (reason != null)
			return reason;
		return httpStatusCode.getMessage();
	}
	
	/**
	 * Internal function for building the HTTP header body of the response.
	 * @return
	 */
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
	
	/**
	 * Helper function for building a simple yet servable HTML page for any error or non-content status codes.
	 * @return
	 */
	public String generateErrorPage() {
		String result = String.format("%d - %s", httpStatusCode.getCode(), getReason()); // fallback;
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
	
	/**
	 * Assembles HttpResponse as a single string in form of an array of bytes, so it can be
	 * sent to client.
	 * @return
	 */
	public byte[] getBytes()
	{
		/* Header */
		String header = buildHeader();
		int headLength = header.length();
		
		/* Content */
		byte[] content = new byte[0];
		if (hasField("Content-Data"))
			content = HttpParser.getArbitraryData(getField("Content-Data"));
		
		/* Put both in a single array */
		byte[] bytes = new byte[headLength + content.length];
		System.arraycopy(header.getBytes(), 0, bytes, 0, headLength);
		System.arraycopy(content, 0, bytes, headLength, content.length);
		
		return bytes;
	}
	
	/**
	 * Returns code this response is associated with.
	 * @return
	 */
	public HttpStatusCode getCode()
	{
		return httpStatusCode;
	}
	
	/**
	 * Returns the full header and body for this HttpResponse.
	 */
	public String toString()
	{
		return buildHeader() + getField("Content-Data");
	}
}

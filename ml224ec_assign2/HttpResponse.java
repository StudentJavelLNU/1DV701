package ml224ec_assign2;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
	
	private static final String CRLF = "\r\n";
	
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
	
	private byte[] content = new byte[0];
	private String contentType;
	
	private String redirectLocation;

	HttpResponse(int statusCode)
	{
		this.statusCode = statusCode;
	}
	
	HttpResponse(int statusCode, String reason)
	{
		this(statusCode);
		
		this.reason = reason;
	}
	
	public void setRedirectLocation(String path)
	{
		redirectLocation = path;
	}
	
	public void setContent(String content, String contentType)
	{
		setContent(content.getBytes(), contentType);
	}
	
	public void setContent(byte[] content, String contentType)
	{
		this.content = content;
		this.contentType = contentType;
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
		builder.append("Server: ml224ec's Simple Web Server" + CRLF);
		if (statusCode == 302)
			builder.append("Location: " + redirectLocation + CRLF);
		else if (statusCode == 200)
		{
			builder.append("Content-Length: " + content.length + CRLF);
			builder.append("Content-Type: " + contentType + CRLF);
		}
		builder.append(CRLF);
		
		return builder.toString();
	}
	
	public byte[] getBytes()
	{
		String header = buildHeader();
		int headLength = header.length();
		
		byte[] bytes = new byte[headLength + content.length];
		System.arraycopy(header.getBytes(), 0, bytes, 0, headLength);
		System.arraycopy(content, 0, bytes, headLength, content.length);
		
		return bytes;
	}
	
	public String toString()
	{
		return buildHeader() + new String(content);
	}
}

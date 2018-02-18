package ml224ec_assign2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class RequestHandler implements Runnable {
	
	
	private static final String DEFAULT_HTML_PAGE = "index.html";

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
	
	private final Socket remote;
	
	private InputStream in;
	private OutputStream out;
	
	RequestHandler(Socket remote)
	{
		this.remote = remote;	
	}
	
	@Override
	public void run() 
	{
		try {
			in = remote.getInputStream();
			out = remote.getOutputStream();
			
			byte[] buffer = new byte[1024];
			StringBuilder builder = new StringBuilder();
			while (in.available() > 0)
			{
				int read = in.read(buffer, 0, buffer.length);
				/* 8-bit encoding to prevent JVM from corrupting arbitary binary data */
				String str = new String(buffer, "ISO-8859-15").substring(0, read);
				builder.append(str);
			}
			
			String requestString = builder.toString().trim();
			/* Google Chrome likes to send invaild messages that contain only CR-LF */
			if (requestString.isEmpty())
			{
				remote.close();
				return;
			}
			
			//System.out.println(requestString);
			
			HttpRequest request = new HttpRequest(requestString);
			String method = request.getMethod();
			System.out.println(method);
			
			Path searchPath = Paths.get(WebServer.CONTENT_PATH + request.getRequestLocation());
			if (searchPath.endsWith("/") || 
					searchPath.toFile().isDirectory()) // Look for index file
				searchPath = Paths.get(
						searchPath.toString() + "/" + DEFAULT_HTML_PAGE);
			
			if (!AccessPolicy.accessAllowed(searchPath.toString()))
			{
				sendResponse(new HttpResponse(403));
			}
			else if (method.equals("GET"))
			{	
				if (Files.exists(searchPath))
				{
					String ext = getFileExtension(searchPath, true);
					byte[] content = Files.readAllBytes(searchPath);
					
					HttpResponse response = new HttpResponse(200); // OK
					response.setContent(content, MEDIA_TYPES.get(ext));
					
					sendResponse(response);
				}
				else
					sendResponse(new HttpResponse(404)); // File not found
			}
			else if (method.equals("POST") || method.equals("PUT"))
			{
				HttpResponse response = ImageUploadService.completeRequest(request);
				
				sendResponse(response);
			}
			else
				sendResponse(new HttpResponse(502)); // Method not supported
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			try {
				sendResponse(new HttpResponse(500)); // Internal server error
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		try {
			remote.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private String getFileExtension(Path path, boolean noDot)
	{
		if (path.endsWith("/"))
			return "";
		
		String n = path.getFileName().toString();		
		return n.substring(n.indexOf('.')+ (noDot ? 1 : 0));
	}
	
	private void sendResponse(HttpResponse httpResponse) throws IOException
	{
		out.write(httpResponse.getBytes());
		if (WebServer.DEBUG)
			System.out.printf("Replied with %s\n", httpResponse.getCode());
	}
	
	/*
	private void sendResponse(byte[] content, String contentType)
	{
		String response 
		= "HTTP/1.1 " + "200 OK" + "\r\n"
		+ "Server: ml224ec's Assignment 2 Web Server\r\n"
		+ "Content-Length: " + content.length + "\r\n"
		+ "Connection: close\r\n"
		+ "Content-Type: " + contentType + "\r\n"
		+ "\r\n";
		
		try {
			out.write(response.getBytes());
			out.write(content);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void sendResponse(String content, String contentType)
	{		
		sendResponse(content.getBytes(), contentType);
	}
	*/

}

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

	private static final HashMap<String, String> map = 
			new HashMap<String, String>()
	{
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
			
			byte[] buffer = new byte[256];
			StringBuilder builder = new StringBuilder();
			while (in.available() > 0)
			{
				int read = in.read(buffer, 0, buffer.length);
				builder.append(new String(buffer, 0, read));
			}
			
			String requestString = builder.toString();
			
			String[] parts = requestString.split("\\r\\n");
			
			System.out.println(parts[0]);
			
			String[] request = parts[0].split(" ");
			
			String method = request[0];
			String path = 	request[1];
			String type = 	request[2];
			
			if (request[0].equals("GET") || request[0].equals("POST"))
			{
				Path searchPath = Paths.get(WebServer.CONTENT_PATH + path);
				if (path.endsWith("/")) // Look for index file
					searchPath = Paths.get(
							searchPath.toString() + "/" + DEFAULT_HTML_PAGE);
				
				System.out.println(searchPath.toAbsolutePath());
				if (Files.exists(searchPath))
				{
					String ext = getFileExtension(searchPath, true);
					System.out.println(ext);
					byte[] content = Files.readAllBytes(searchPath);
					sendResponse(content, map.get(ext));
					
				}
				else
				{
					sendResponse("404 - File not found", map.get("html"));
				}
				/*
				sendResponse("<html><head></head><body><h1>Hello world!</h1></body></html>", "text/html");
				System.out.println("Response sent!");
				*/
			}
			else
			{
				sendResponse("502 - Method is not implemented", map.get("html"));
			}
			
			remote.close();
			//System.out.printf("%s\n", builder.toString().trim());
		}
		catch (Exception e)
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

}

package ml224ec_assign2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A thread-safe class for handling HTTP requests with multi-threading.
 * @author Martin Lyrå
 *
 */
public class RequestHandler implements Runnable {
	
	/**
	 * Name of index HTML file
	 */
	private static final String DEFAULT_HTML_PAGE_NAME = "index";
	
	private final Socket remote;
	
	private InputStream in;
	private OutputStream out;
	
	RequestHandler(Socket remote)
	{
		this.remote = remote;	
	}
	
	/**
	 * Run the actual handling of requests.
	 */
	@Override
	public void run() 
	{
		try { /* <- Try-Catch for socket closing */
			try { /* <- Try-Catch for actual internal server errors */
				
				/* Set up streams */
				in = remote.getInputStream();
				out = remote.getOutputStream();
				
				/* Prepare buffer and string builder to read input data */
				byte[] buffer = new byte[1024];
				StringBuilder builder = new StringBuilder();
				while (in.available() > 0)
				{
					int read = in.read(buffer, 0, buffer.length);
					/* 8-bit encoding to prevent JVM from corrupting arbitary binary data */
					String str = new String(buffer, "ISO-8859-15").substring(0, read);
					builder.append(str);
				}
				
				/* Complete the string pieces into one string */
				String requestString = builder.toString().trim();
				
				/* Google Chrome likes to send invaild messages that contain only CR-LF */
				if (requestString.isEmpty())
				{
					remote.close();
					return;
				}
				
				/* Turn the string into an object to make the data more accessible */
				HttpRequest request = new HttpRequest(requestString);
				String method = request.getMethod();
				if (WebServer.DEBUG)
					System.out.println(method);
				
				/* Turn the requested path into a full path for the server to locate */
				Path searchPath = Paths.get(WebServer.CONTENT_PATH + request.getRequestLocation());
				
				/* Should the server serve the request with index.htm(l)? */
				if (searchPath.endsWith("/") || 
						searchPath.toFile().isDirectory()) // Look for index file
					searchPath = getHtmlFile(searchPath, DEFAULT_HTML_PAGE_NAME);
				
				/* Check if the access policy allows access to the requested file/dir */
				if (!AccessPolicy.accessAllowed(searchPath.toString()))
				{
					sendResponse(new HttpResponse(403));
				}
				
				/* Handle the HTTP request by supported methods GET, POST, and PUT */
				else if (method.equals("GET"))
				{	
					if (Files.exists(searchPath))
					{
						/* Serve the client the requested content */
						
						/* Get extension and arbitary data */
						String ext = getFileExtension(searchPath, true);
						byte[] content = Files.readAllBytes(searchPath);
						
						/* Form a HTTP response with the acquired data and extension */
						HttpResponse response = new HttpResponse(200); // OK
						response.setContent(content, HttpParser.getContentType(ext));
						
						/* Send the reply */
						sendResponse(response);
					}
					else
						sendResponse(new HttpResponse(404)); // File not found
				}
				else if (method.equals("POST") || method.equals("PUT"))
				{
					/* Task 2 - Serve POST and PUT requests (and uploads) as if server is an
					 * image uploading service.
					 */
					HttpResponse response = ImageUploadService.completeRequest(request);
					
					/* Send the reply */
					sendResponse(response);
				}
				else
					sendResponse(new HttpResponse(502)); // Method not supported
			}
			/* Internal error */
			catch (Exception e)
			{
				e.printStackTrace();
				
				sendResponse(new HttpResponse(500)); // Internal server error
			}
			/* Close the socket last, no matter what */
			remote.close();
		} catch (Exception e)
		{
			// e.printStackTrace(); // I don't think we do have need for this line
		}
	}
	
	/**
	 * Returns the file extension by performing a substring on 'path' and returns that piece.
	 * Specify noDot as true if you do not want to include the dot in the resulting string.
	 * Returns an empty string if the file has no extension.
	 * @param path
	 * @param noDot
	 * @return
	 */
	private String getFileExtension(Path path, boolean noDot)
	{
		if (path.endsWith("/"))
			return "";
		
		String n = path.getFileName().toString();
		int i = n.indexOf('.');
		
		if (i > -1)
			return n.substring(i + (noDot ? 1 : 0));
		return "";
	}
	
	/**
	 * Looks for if there exists (filename).htm. If it does, return the path to it.
	 * Else if it doesn't exist, a path to (filename).html is returned instead.
	 * 
	 * A rather ugly and lazy workround to the index file problem, in my opinion.
	 * @param directory
	 * @param filename
	 * @return
	 */
	private Path getHtmlFile(Path directory, String filename)
	{
		Path p = Paths.get(directory.toString() + '/' + filename + ".htm");
		if( Files.exists(p))
			p = Paths.get(directory.toString() + '/' + filename + ".html");
		return p;		
	}
	
	/**
	 * Turns HttpResponse into an array of bytes, sending it back to the who sent the request.
	 * @param httpResponse
	 * @throws IOException
	 */
	private void sendResponse(HttpResponse httpResponse) throws IOException
	{
		out.write(httpResponse.getBytes());
		if (WebServer.DEBUG)
			System.out.printf("Replied with %s\n", httpResponse.getCode());
	}
}

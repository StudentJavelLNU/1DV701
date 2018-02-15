package ml224ec_assign2;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Path;

public class WebServer {
	
	public static final boolean DEBUG = true;
	public static final int DEFAULT_PORT = 8888;
	
	public static final String CONTENT_PATH = "ml224ec_assign2/web-content";

	public static void main(String[] args) 
	{			
		WebServer server = new WebServer();
		
		server.start();
	}

	public void start()
	{
		try {
			ServerSocket socket = new ServerSocket();
			SocketAddress local = new InetSocketAddress(DEFAULT_PORT);
			
			socket.bind(local);
			if (DEBUG)
				socket.setReuseAddress(true);
			
			System.out.printf("Ready! Listening on port %d.\n", socket.getLocalPort());
			
			while (true) {
				Socket request = socket.accept();
				
				new Thread(new RequestHandler(request)).start();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}

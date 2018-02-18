package ml224ec_assign2;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

public class WebServer {
	
	public static final boolean DEBUG = true;
	public static final int DEFAULT_PORT = 8888;
	
	/* change the last digit to '2' for Task 2 web content */
	public static final String CONTENT_PATH = "ml224ec_assign2/web-content-2";
	public static final String TEMPLATE_PATH = "ml224ec_assign2/templates";
	
	public static boolean running;

	public static void main(String[] args) 
	{			
		AccessPolicy.initialize();
		
		WebServer server = new WebServer();
		
		server.start();
	}

	public void start()
	{
		running = true;
		try {
			ServerSocket socket = new ServerSocket();
			SocketAddress local = new InetSocketAddress(DEFAULT_PORT);
			
			socket.bind(local);
			if (DEBUG)
				socket.setReuseAddress(true);
			
			System.out.printf("Ready! Listening on port %d.\n", socket.getLocalPort());
			
			while (running) {
				Socket request = socket.accept();
				
				new Thread(new RequestHandler(request)).start();
			}
			
			socket.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}

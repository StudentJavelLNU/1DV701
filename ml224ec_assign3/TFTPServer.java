package ml224ec_assign3;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

public class TFTPServer 
{
	public static final boolean DEBUG = true;
	public static final boolean OVERWRITE = false;
	
	public static final int PORT = 4970;
	public static final int BUFFER_SIZE = 516;

	public static void main(String[] args) {
		if (args.length > 0) 
		{
			System.err.printf("usage: java %s\n", TFTPServer.class.getCanonicalName());
			System.exit(1);
		}
		//Starting the server
		try 
		{
			TFTPServer server= new TFTPServer();
			server.start();
		}
		catch (SocketException e) 
			{e.printStackTrace();}
	}
	
	private void start() throws SocketException 
	{
		boolean running = true;
		byte[] buffer= new byte[BUFFER_SIZE];
		
		// Create socket
		DatagramSocket socket= new DatagramSocket(null);
		
		// Create local bind point 
		SocketAddress localBindPoint= new InetSocketAddress(PORT);
		socket.bind(localBindPoint);

		System.out.printf("Listening at port %d for new requests\n", PORT);

		// Loop to handle client requests 
		while (running) 
		{        
			try {
				/* Receive request */
				final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				
				socket.receive(packet);
				
				final InetSocketAddress clientAddress = 
						new InetSocketAddress(packet.getAddress(), packet.getPort());
				
				/* Handle the request packet in a new connection and thread */
				new Thread(
						new TFTPHandler(new DatagramSocket(0), clientAddress, packet)
						).start();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		socket.close();
	}
}




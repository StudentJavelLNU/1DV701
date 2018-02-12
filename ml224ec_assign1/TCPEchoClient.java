package ml224ec_assign1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class TCPEchoClient extends NetLayer {

	public static final String MSG= "An Echo Message!";
	
	private Socket socket;
	
	/**
	 * Main entry for TCPEchoClient as a program
	 * @param args
	 */
	public static void main(String[] args) 
	{
		try {
			if (args == null || args.length != 5)
			{
				System.out.print("Usage: \"remote-IP-address\" remote-port local-port buffer-size rate\n");
			}
			else
			{
				// Assign arguments
				TCPEchoClient client = new TCPEchoClient(
						args[0],
						Integer.parseInt(args[1]),
						Integer.parseInt(args[2]),
						Integer.parseInt(args[3]),
						Integer.parseInt(args[4])
						); 
				client.start(); // start working
			}
		}
		catch (Exception e)
		{
			System.out.printf("Error: %s\n", e.getMessage());
		}
	}
	
	public TCPEchoClient(String remoteAddress, int remotePort, int localPort, int bufferSize, int transferRate) {
		super(remoteAddress, remotePort, localPort, bufferSize, transferRate);
	}

	@Override
	protected void start() throws IOException 
	{
		socket = new Socket();
		try {
			socket.bind(localAddress);
			socket.connect(remoteAddress);
			
			try {
				//run(); // run forever
				runForASecond(); // run for a second
				socket.close();
			} catch (SocketException e)
			{
				System.out.printf("Connection to %s closed. (%s)\n", socket.getInetAddress().getHostAddress(), e.getMessage());
			}
		}
		catch (IOException e)
		{
			System.out.printf("Error! Unable to connect: %s\n", e.getMessage());
			socket.close();
			return;
		}
	}
	
	@Override
	protected void tick() throws IOException 
	{	
		// Set up streams for net I/O
		OutputStream out = socket.getOutputStream();
		InputStream in = socket.getInputStream();
		
		// Send
		out.write(MSG.getBytes(), 0, MSG.length());
		
		// Receive
		String receivedPart;
		StringBuilder stringBuilder = new StringBuilder();
		do {
			int readBytes = in.read(buffer, 0, bufferSize); // Read, number of bytes read assigned to readBytes
			receivedPart = new String(buffer, 0, readBytes); // Copy from buffer
			stringBuilder.append(receivedPart); // Add to builder
		} while (in.available() > 0);
		// End Receive	
		
		// Assemble the received message
		String receivedString = stringBuilder.toString();
		
		// Print output
		System.out.printf("%d bytes sent and %d bytes received\n", MSG.length(), receivedString.length());
		
		// Compare
		int res = receivedString.compareTo(MSG);
		if (res != 0)
		    System.out.printf("Sent and received message strings are not equal! (Delta: %d)\n", res);
	}

}

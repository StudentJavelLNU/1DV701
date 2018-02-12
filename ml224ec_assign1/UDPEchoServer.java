/*
  UDPEchoServer.java
  A simple echo server with no error handling
*/

package ml224ec_assign1; // Critical edit
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * 
 * @author Martin Lyrå
 *
 */
public class UDPEchoServer extends NetLayer {

	public static final int DEFAULT_BUFFER_SIZE = 1024;
	public static final int DEFAULT_PORT = 4950;
	
	public static void main(String[] args)
	{
		try {
			int port = DEFAULT_PORT;
			int bufferSize = DEFAULT_BUFFER_SIZE;
			
			if (args.length > 0)
				port = Integer.parseInt(args[0]);
			if (args.length > 1)
				bufferSize = Integer.parseInt(args[1]);
			
			UDPEchoServer server = new UDPEchoServer(
						port, bufferSize, 0
					);
			server.start();
		}
		catch (Exception e)
		{
			System.out.printf("Error: %s\n", e.toString());
		}
	}
	
	public UDPEchoServer(int localPort, int bufferSize, int transferRate) {
		super(localPort, bufferSize, transferRate);
	}
    
	@Override
    protected void start() throws IOException 
    {
		/* Create socket */
		DatagramSocket socket= new DatagramSocket(null);
		socket.bind(localAddress);
		
		while (true) {
		    /* Create datagram packet for receiving message */
		    DatagramPacket receivePacket= new DatagramPacket(buffer, bufferSize);
	
		    /* Receiving message */
		    socket.receive(receivePacket);
	
		    /* Create datagram packet for sending message */
		    DatagramPacket sendPacket=
			new DatagramPacket(receivePacket.getData(),
					   receivePacket.getLength(),
					   receivePacket.getAddress(),
					   receivePacket.getPort());
	
		    /* Send message*/
		    socket.send(sendPacket);
		    System.out.printf("UDP Echo: %d bytes received from %s:%d, responded with %d bytes.\n",
		    		receivePacket.getLength(),
		    		receivePacket.getAddress().getHostAddress(),
		    		receivePacket.getPort(),
		    		sendPacket.getLength());
		}
    }

	@Override
	protected void tick() throws IOException {
		// TODO Auto-generated method stub
		
	} 
}
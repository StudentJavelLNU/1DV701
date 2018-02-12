/*
  UDPEchoClient.java
  A simple echo client with no error handling
*/

package ml224ec_assign1; // Critical edit
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * 
 * @author Martin Lyrå
 *
 */
public class UDPEchoClient extends NetLayer {

    public static final String MSG= "An Echo Message!";
    
    private DatagramSocket socket;

    public static void main(String[] args)
    {
    	try {
			if (args == null || args.length != 5)
			{
				System.out.print("Usage: \"remote-IP-address\" remote-port local-port buffer-size rate\n");
			}
			else
			{
				UDPEchoClient client = new UDPEchoClient(
						args[0],
						Integer.parseInt(args[1]),
						Integer.parseInt(args[2]),
						Integer.parseInt(args[3]),
						Integer.parseInt(args[4])
						); 
				client.start();
			}
    	}
    	catch (Exception e)
    	{
    		System.out.printf("Error: %s\n", e.getMessage());
    	}
    }
    
    public UDPEchoClient(String remoteAddress, int remotePort, int localPort, int bufferSize, int transferRate) {
		super(remoteAddress, remotePort, localPort, bufferSize, transferRate);
	}
    
    public void start() throws IOException
    {
		/* Create socket */
		socket= new DatagramSocket(localAddress);
		
		runForASecond();
		
		socket.close();
    }
    
    /**
     * A work cycle for UDPEchoClient
     * @see ml224ec_assign1.NetLayer#tick()
     */
    @Override
    protected void tick() throws IOException
    {
    	/* Create datagram packet for sending message */
		DatagramPacket sendPacket=
		    new DatagramPacket(MSG.getBytes(),
				       MSG.length(),
				       remoteAddress);
		
		/* Create datagram packet for receiving echoed message */
		DatagramPacket receivePacket= new DatagramPacket(buffer, bufferSize);
		
		/* Send and receive message*/
		socket.send(sendPacket);
		socket.receive(receivePacket);
		
		String receivedString=
		    new String(receivePacket.getData(),
			       receivePacket.getOffset(),
			       receivePacket.getLength());
		
		System.out.printf("%d bytes sent and %d bytes received\n", sendPacket.getLength(), receivePacket.getLength());
		
		/* Compare sent and received message */
		int res = receivedString.compareTo(MSG);
		if (res != 0)
		    System.out.printf("Sent and received message strings are not equal! (Delta: %d)\n", res);
    }
}
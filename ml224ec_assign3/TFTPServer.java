package ml224ec_assign3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

import ml224ec_assign3.tftp.Operation;

public class TFTPServer 
{
	public static final boolean DEBUG = true;
	public static final boolean OVERWRITE = true;
	
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
		byte[] buffer= new byte[BUFFER_SIZE];
		
		// Create socket
		DatagramSocket socket= new DatagramSocket(null);
		
		// Create local bind point 
		SocketAddress localBindPoint= new InetSocketAddress(PORT);
		socket.bind(localBindPoint);

		System.out.printf("Listening at port %d for new requests\n", PORT);

		// Loop to handle client requests 
		while (true) 
		{        
			try {
				final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				
				socket.receive(packet);
				
				final InetSocketAddress clientAddress = 
						new InetSocketAddress(packet.getAddress(), packet.getPort());
				
				new Thread(
						new TFTPHandler(new DatagramSocket(0), clientAddress, packet)
						).start();
				
				/*
				new Thread() 
				{
					public void run() 
					{
						try 
						{
							DatagramSocket sendSocket= new DatagramSocket(0);
	
							// Connect to client
							sendSocket.connect(clientAddress);
							
							sendSocket.setSoTimeout(SOCKET_TIMEOUT_MS); // 3 secs
							
							System.out.printf("%s request for %s from %s using port %d\n",
									(reqtype == Operation.READ_REQUEST)?"Read":"WriteRequestHandler", file,
									clientAddress.getHostName(), clientAddress.getPort());  
									
							// Read request
							if (reqtype == Operation.READ_REQUEST) 
							{      
								requestedFile.insert(0, READDIR);
								HandleRQ(sendSocket, requestedFile.toString(), Operation.READ_REQUEST);
							}
							// WriteRequestHandler request
							else 
							{                       
								requestedFile.insert(0, WRITEDIR);
								HandleRQ(sendSocket, requestedFile.toString(), Operation.WRITE_REQUEST);  
							}
							sendSocket.close();
						} 
						catch (IOException e) 
							{e.printStackTrace();
						}
					}
				}.start();*/
			}
			catch (Exception e)
			{
				
			}
		}
	}
	
	/**
	 * Reads the first block of data, i.e., the request for an action (read or write).
	 * @param socket (socket to read from)
	 * @param buf (where to store the read data)
	 * @return socketAddress (the socket address of the client)
	 * @throws IOException 
	 */
	private InetSocketAddress receiveFrom(DatagramSocket socket, byte[] buf) throws IOException 
	{
		// Create datagram packet
		DatagramPacket dp = new DatagramPacket(buf, buf.length); 
		
		// Receive packet
		socket.receive(dp);
		
		// Get client address and port from the packet
		return new InetSocketAddress(dp.getAddress(), dp.getPort());
	}

	/**
	 * Parses the request in buf to retrieve the type of request and requestedFile
	 * 
	 * @param buf (received request)
	 * @param requestedFile (name of file to read/write)
	 * @return opcode (request type: RRQ or WRQ)
	 */
	private Operation ParseRQ(byte[] buf, StringBuffer requestedFile) 
	{
		// See "TFTP Formats" in TFTP specification for the RRQ/WRQ request contents
		ByteBuffer bb = ByteBuffer.wrap(buf);
		
		short opcode = bb.getShort(); // read 16 bits (2 bytes)
		
		/* End of the request String contains parameters seperated by null terminators */
		String[] params = new String(buf, bb.position(), bb.remaining()).split("\0");
		
		/* We just want the filename for now */
		String filename = params[0];
		
		requestedFile.append(filename);
		
		return Operation.valueOf(opcode);
	}
}




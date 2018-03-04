package ml224ec_assign3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import ml224ec_assign3.tftp.Error;
import ml224ec_assign3.tftp.Operation;
import ml224ec_assign3.tftp.exceptions.FileExistsException;
import ml224ec_assign3.tftp.exceptions.FileNotFoundException;
import ml224ec_assign3.tftp.exceptions.IllegalOperationException;
import ml224ec_assign3.tftp.exceptions.OutOfSpaceException;
import ml224ec_assign3.tftp.exceptions.TFTPException;
import ml224ec_assign3.tftp.exceptions.TimeoutException;

public class TFTPServer 
{
	public static final int PORT = 4970;
	public static final int DATA_BUFFER_SIZE = 512;
	public static final int PACKET_BUFFER_SIZE = DATA_BUFFER_SIZE + 4;
	public static final int SOCKET_TIMEOUT_MS = 1000; // 3 secs
	public static final int MAX_ATTEMPTS = 5;
	
	public static final String READDIR = "read/";
	public static final String WRITEDIR = "write/";
	
	public static final Map<Short, TFTPHandler> handlers =
			new HashMap<Short, TFTPHandler>();

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
		byte[] buf= new byte[PACKET_BUFFER_SIZE];
		
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
				final InetSocketAddress clientAddress = receiveFrom(socket, buf);
				
				// If clientAddress is null, an error occurred in receiveFrom()
				if (clientAddress == null) 
					continue;
	
				final StringBuffer requestedFile= new StringBuffer();
				final Operation reqtype = ParseRQ(buf, requestedFile);
				final String file = requestedFile.toString();
				
				boolean newConnection = false;
				
				if (newConnection)
				{
					final TFTPHandler handler = new TFTPHandler(new DatagramSocket(0), clientAddress);
					handlers.put((short) clientAddress.getPort(), handler);
					new Thread(handler).start();
				}
				
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
									(reqtype == Operation.READ_REQUEST)?"Read":"Write", file,
									clientAddress.getHostName(), clientAddress.getPort());  
									
							// Read request
							switch (reqtype)
							{
							case READ_REQUEST: {
								requestedFile.insert(0, READDIR);
								HandleRQ(sendSocket, requestedFile.toString(), Operation.READ_REQUEST);
								break;
							}
							case WRITE_REQUEST: {
								requestedFile.insert(0, WRITEDIR);
								HandleRQ(sendSocket, requestedFile.toString(), Operation.WRITE_REQUEST);  
								break;
							}
							default: case ERROR: {
								break;
							}
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

	/**
	 * Handles RRQ and WRQ requests 
	 * 
	 * @param sendSocket (socket used to send/receive packets)
	 * @param requestedFile (name of file to read/write)
	 * @param opcode (RRQ or WRQ)
	 * @throws IOException 
	 */
	private void HandleRQ(DatagramSocket sendSocket, String requestedFile, Operation opcode) 
			throws IOException 
	{	
		Path p = Paths.get(requestedFile);
		try {
			if(opcode == Operation.READ_REQUEST)
			{	
				if (Files.exists(p))
				{
					System.out.printf("Sending data for %s\n", requestedFile);
					handleReadRequest(sendSocket, requestedFile);
				}
				else
					throw new FileNotFoundException();
			}
			else if (opcode == Operation.WRITE_REQUEST) 
			{
				if (!Files.exists(p))
				{
					System.out.printf("Receiving data to write %s\n", requestedFile);
					handleWriteRequest(sendSocket, requestedFile);
				}
				else
					throw new FileExistsException();
			}
			else 
				throw new IllegalOperationException("Illegal request");
		} catch (TFTPException e)
		{
			String message = e.getMessage();
			String operation = opcode == Operation.READ_REQUEST ? "read" : "write";
			if (e instanceof IllegalOperationException)
				operation = "illegal";
			
			System.out.printf("Error serving %s request: %s\n", operation, message);
			sendError(sendSocket, e.getAssociatedErrorCode(), e.getMessage());
		}
	}
	
	/**
	 * Handles RRQs (read requests) proper
	 * 
	 * Previously named "send_DATA_receive_ACK"
	 * @param socket
	 * @param file
	 * @throws IOException
	 * @throws TimeoutException 
	 */
	private void handleReadRequest(DatagramSocket socket, String file)
			throws IOException, TimeoutException
	{
		byte[] dataBuffer = readAllBytes(file);
		
		int remainingLength = dataBuffer.length;
		short block = 1;
		
		do {
			int dataLength = DATA_BUFFER_SIZE;
			if (remainingLength < DATA_BUFFER_SIZE)
			{
				if (remainingLength < 0)
					dataLength = 0;
				else
					dataLength = remainingLength;
			}
			
			// DAT
			ByteBuffer bb = ByteBuffer.allocate(4 + dataLength);
			int length = bb.limit();
			
			bb.putShort(Operation.DATA.getCode());
			bb.putShort(block);
			bb.put(arrayRegion(dataBuffer, (block-1)*DATA_BUFFER_SIZE, dataLength));
			
			DatagramPacket packet = new DatagramPacket(bb.array(), length);
			
			/* Send data, and receive ACK, retransit until a correct ACK has been received */
			int attempt = 0;
			Operation code = null;
			short acknowledgedBlock = 0;
			do
			{	
				if (attempt >= MAX_ATTEMPTS)
					throw new TimeoutException();
				
				socket.send(packet);
				
				try {
					// ACK
					byte[] buffer = new byte[4];
					receiveFrom(socket, buffer);
					
					bb = ByteBuffer.wrap(buffer);
					
					code = Operation.valueOf(bb.getShort());
					acknowledgedBlock = bb.getShort();
				} catch (SocketTimeoutException e) {  }

				attempt++;
			}
			while (block != acknowledgedBlock);
			
			System.out.printf("Block #%d sent and acknowledged (%d bytes)\n", block, length);
			
			remainingLength -= DATA_BUFFER_SIZE;
			block++;
		} while (remainingLength >= 0);
	}
	
	/**
	 * Handles WRQs (write requests) proper
	 *
	 * Previously named "receive_DATA_send_ACK"
	 * @param socket
	 * @param file
	 * @throws IOException
	 * @throws TFTPException 
	 */
	private void handleWriteRequest(DatagramSocket socket, String file)
			throws IOException, TFTPException
	{
		StringBuffer sb = new StringBuffer();
		
		short block = 0;
		short lastBlock = 0;
		
		int attempt = 0;
		int length = 0;
		int dataLength = 0;
		int receivedDataBytes = 0;
		
		boolean endOfTransfer = false;
		do 
		{
			if (attempt >= MAX_ATTEMPTS)
				throw new TimeoutException();
			
			// ACK
			acknowledge(socket, lastBlock);
			
			// DAT (if expecting more)
			if (!endOfTransfer)
			{
				/* Try to receive Data from client */
				try {
					byte[] buffer = new byte[PACKET_BUFFER_SIZE];
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					ByteBuffer bb = ByteBuffer.wrap(buffer);
					
					socket.receive(packet);
					
					Operation code = Operation.valueOf(bb.getShort());
					block = bb.getShort();
					
					if (code != Operation.DATA)
						throw new IllegalOperationException();
					
					/* Parse DAT packet */
					length = packet.getLength();
					dataLength = length - 4;
					
					sb.append(new String(bb.array(), bb.position(), dataLength).toString());
					
					/* Update transfer info */
					receivedDataBytes += dataLength;
					lastBlock = block;
					
					endOfTransfer = 
							(dataLength == 0 || receivedDataBytes % DATA_BUFFER_SIZE > 0);
					
					System.out.printf("Block #%d received (%d bytes)\n", block, length);
				} catch (SocketTimeoutException e) {}
			} else
				break;
			
			attempt++;
		} while (block <= lastBlock);
			
		writeToFile(file, sb.toString().getBytes());
	}
	
	/**
	 * 
	 * 
	 * Previouly named "send_ERR"
	 * @param socket
	 * @param errorCode
	 * @param message
	 * @throws IOException
	 */
	private void sendError(DatagramSocket socket, Error errorCode, String message)
			throws IOException
	{
		ByteBuffer bb = ByteBuffer.allocate(4 + message.length() + 1);
		
		bb.getShort(Operation.ERROR.getCode());
		bb.putShort(errorCode.getCode());
		bb.put(message.getBytes());
		bb.put((byte) 0);
		
		socket.send(new DatagramPacket(bb.array(), bb.limit()));
	}
	
	private byte[] readAllBytes(String file) throws IOException
	{
		try {
			return Files.readAllBytes(Paths.get(file));
		} catch (IOException e) {
			throw e;
		}
	}
	
	/**
	 * Write [all] bytes to destination file
	 * @param file
	 * @param data
	 * @throws IOException
	 * @throws TFTPException
	 */
	private void writeToFile(String file, byte[] data) 
			throws IOException, TFTPException
	{
		if (getFreeDiskSpaceBytes() < data.length)
			throw new OutOfSpaceException();
		try (FileOutputStream os = new FileOutputStream(file))
		{
			os.write(data);
		} 
	}
	
	/**
	 * Sends an ACK packet to remote destination, must have the block 
	 * variable specified.
	 * @param socket
	 * @param block
	 * @throws IOException
	 */
	private void acknowledge(DatagramSocket socket, short block)
			throws IOException
	{
		ByteBuffer bb = ByteBuffer.wrap(new byte[4]);
		
		bb.putShort(Operation.ACKNOWLEDGE.getCode());
		bb.putShort(block);
		
		socket.send(new DatagramPacket(bb.array(), 4));
	}
	
	/**
	 * Returns a "splice" copy of a region in the given array, specified with
	 * offset and length
	 * @param source
	 * @param offset
	 * @param length
	 * @return
	 */
	private byte[] arrayRegion(byte[] source, int offset, int length)
	{
		byte[] destination = new byte[length];
		System.arraycopy(source, offset, destination, 0, length);
		return destination;
	}
	
	private long getFreeDiskSpaceBytes()
	{
		return new File("").getFreeSpace();
	}
}




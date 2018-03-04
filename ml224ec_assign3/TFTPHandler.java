package ml224ec_assign3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import ml224ec_assign3.tftp.AbstractRequestHandler;
import ml224ec_assign3.tftp.Error;
import ml224ec_assign3.tftp.Operation;
import ml224ec_assign3.tftp.ReadRequestHandler;
import ml224ec_assign3.tftp.TFTP;
import ml224ec_assign3.tftp.TFTPPacket;
import ml224ec_assign3.tftp.WriteRequestHandler;
import ml224ec_assign3.tftp.exceptions.ErrorPacketException;
import ml224ec_assign3.tftp.exceptions.FileExistsException;
import ml224ec_assign3.tftp.exceptions.FileNotFoundException;
import ml224ec_assign3.tftp.exceptions.IllegalOperationException;
import ml224ec_assign3.tftp.exceptions.OutOfSpaceException;
import ml224ec_assign3.tftp.exceptions.TFTPException;
import ml224ec_assign3.tftp.exceptions.TimeoutException;
import ml224ec_assign3.tftp.exceptions.UnknownTransferIDException;

public class TFTPHandler implements Runnable {

	final int SOCKET_TIMEOUT_MS = 1000; // 3 secs
	final int MAX_ATTEMPTS = 5;
	
	final String READDIR = "read/";
	final String WRITEDIR = "write/";
	
	final DatagramSocket socket;
	final InetSocketAddress remoteAddress;
	
	final Operation requestType;
	final String targetFile;
	final List<String> parameters;
	final AbstractRequestHandler handler;
	
	TFTPHandler(DatagramSocket socket, InetSocketAddress remoteAddress, DatagramPacket packet)
	{
		this.socket = socket;
		this.remoteAddress = remoteAddress;
		
		this.parameters = new LinkedList<String>();
		Operation requestType = parseRequestPacket(packet, parameters);
		
		this.requestType = requestType;
		this.targetFile = parameters.get(0);
		
		if (requestType == Operation.READ_REQUEST)
			handler = new ReadRequestHandler(READDIR + targetFile);
		else if (requestType == Operation.WRITE_REQUEST)
			handler = new WriteRequestHandler(WRITEDIR + targetFile);
		else
			throw new IllegalArgumentException();
	}
	
	@Override
	public void run() {
		try 
		{
			socket.setSoTimeout(SOCKET_TIMEOUT_MS);
			socket.connect(remoteAddress);
				
			System.out.printf("::: Handling %s for %s :::\n", 
					requestType,
					targetFile);
			try 
			{	
				/* Start the chain reaction by transmitting the first packet */
				handler.start(socket);
				
				/* Receive and handle packets */
				int attempts = 0;
				while (socket.isConnected())
				{
					TFTPPacket packet = receive(socket);
					
					try 
					{
						if (packet != null)
						{
							/* Print a message when debugging */
							if (TFTPServer.DEBUG)
								System.out.printf(
										"%s packet received\n", packet.getOperationCode());
							
							/* Check if TIDs are consistent */
							if (packet.getTransferId() != remoteAddress.getPort())
								throw new UnknownTransferIDException();
							
							/* Check if packet is an error */
							if (packet.getOperationCode() == Operation.ERROR)
								throw new ErrorPacketException();
							
							/* Handle the request */
							handler.handleRequest(socket, packet);
							
							/* Disconnect when the transfer has been finished */
							if (handler.isTransferFinished())
								socket.disconnect();
						}
						else
						{
							/* We would not want our application get stuck re-transmitting */
							if (attempts >= MAX_ATTEMPTS)
								throw new TimeoutException();
							attempts++;
							
							/* Retransmit packet */
							handler.retransmit(socket);
						}
					} 
					/* Do not cut connection on error 6*/
					catch (UnknownTransferIDException e) {
						try (DatagramSocket tmp = new DatagramSocket(packet.getSourceAddress()))
						{
							sendError(tmp, e.getAssociatedErrorCode(), e.getMessage());
						}
					}
				}
			}
			catch (TFTPException e)
			{
				String message = e.getMessage();
				String operation = requestType == Operation.READ_REQUEST ? "read" : "write";
				if (e instanceof IllegalOperationException)
					operation = "illegal";
				
				/* Reply to client, unless client wants to close connection */
				System.out.printf("Error serving %s request: %s\n", operation, message);
				if (!(e instanceof ErrorPacketException))
					sendError(socket, e.getAssociatedErrorCode(), e.getMessage());
				
				socket.disconnect();
			}
			
			socket.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private Operation parseRequestPacket(DatagramPacket packet, List<String> parameters)
	{
		ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
		
		short code = buffer.getShort();
		
		String params = new String(packet.getData(), buffer.position(), buffer.remaining());
		for (String str : params.split("\0"))
			parameters.add(str);
		
		return Operation.valueOf(code);
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
	
	private TFTPPacket receive(DatagramSocket socket) throws IOException
	{
		try {
			byte[] buffer = new byte[TFTP.PACKET_BUFFER_SIZE];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);
			return TFTPPacket.fromDatagram(packet);
		} catch (SocketTimeoutException e)
		{
			return null;
		}
	}
}

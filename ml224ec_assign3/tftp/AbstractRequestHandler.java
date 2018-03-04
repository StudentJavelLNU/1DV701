package ml224ec_assign3.tftp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

import ml224ec_assign3.tftp.exceptions.TFTPException;

/**
 * Abstract request handler object for actually handling requests
 * @author Martin Lyrå
 *
 */
public abstract class AbstractRequestHandler {
	
	protected boolean synced;
	protected boolean transferDone;
	protected short currentBlock;
	protected TFTPPacket lastPacket;
	protected ByteBuffer buffer;
	protected String targetFile;

	protected int packetsSent;
	
	public AbstractRequestHandler(String targetFile)
	{
		this.targetFile = targetFile;
		
		currentBlock = 0;
		lastPacket = null;
		synced = false;
		transferDone = false;
	}
	
	/**
	 * Begins the transfer sequence and handling of the request
	 * @param socket
	 * @throws TFTPException
	 * @throws IOException
	 */
	public void start(DatagramSocket socket) 
			throws TFTPException, IOException
	{
		preStartCheck();
		handleRequest(socket, null, true);
	}
	
	/**
	 * Handle the request
	 * @param socket
	 * @param packet
	 * @throws TFTPException
	 * @throws IOException
	 */
	public void handleRequest(DatagramSocket socket, TFTPPacket packet) 
			throws TFTPException, IOException
	{
		handleRequest(socket, packet, false);
	}
	
	/**
	 * Resends the packet that was last compiled and sent in previous
	 * handleRequest() call.
	 * @param socket
	 * @throws IOException
	 */
	public void retransmit(DatagramSocket socket) 
			throws IOException
	{
		send(socket, lastPacket);
	}
	
	/**
	 * Is request handler done handling the request?
	 * @return
	 */
	public boolean isTransferFinished()
	{
		return synced && transferDone;
	}
	
	/**
	 * Internal function for sending TFTP packets
	 * @param socket
	 * @param packet
	 * @throws IOException
	 */
	protected void send(DatagramSocket socket, TFTPPacket packet) throws IOException
	{
		socket.send(packet.toDatagram());
		lastPacket = packet;
		packetsSent++;
	}
	
	protected abstract void handleRequest(DatagramSocket socket, TFTPPacket packet, boolean firstTime) 
			throws TFTPException, IOException;
	
	/**
	 * Special function for checking and asserting pre-start conditions
	 * individual for each request handler.
	 * @throws TFTPException
	 */
	protected abstract void preStartCheck() throws TFTPException;
	
	public abstract Operation requestTypeHandled();
}

package ml224ec_assign3.tftp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

import ml224ec_assign3.tftp.exceptions.TFTPException;

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
	
	public void start(DatagramSocket socket) 
			throws TFTPException, IOException
	{
		preStartCheck();
		handleRequest(socket, null, true);
	}
	
	public void handleRequest(DatagramSocket socket, TFTPPacket packet) 
			throws TFTPException, IOException
	{
		handleRequest(socket, packet, false);
	}
	
	public void retransmit(DatagramSocket socket) 
			throws IOException
	{
		send(socket, lastPacket);
	}
	
	public boolean isTransferFinished()
	{
		return synced && transferDone;
	}
	
	protected void send(DatagramSocket socket, TFTPPacket packet) throws IOException
	{
		socket.send(packet.toDatagram());
		lastPacket = packet;
		packetsSent++;
	}
	
	protected abstract void handleRequest(DatagramSocket socket, TFTPPacket packet, boolean firstTime) 
			throws TFTPException, IOException;
	
	protected abstract void preStartCheck() throws TFTPException;
	
	public abstract Operation requestTypeHandled();
}

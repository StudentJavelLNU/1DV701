package ml224ec_assign3;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class TFTPHandler implements Runnable {

	final DatagramSocket socket;
	final InetSocketAddress remoteAddress;
	final Queue<DatagramPacket> packets;
	
	TFTPHandler(DatagramSocket socket, InetSocketAddress remoteAddress)
	{
		this.socket = socket;
		this.remoteAddress = remoteAddress;
		packets = new LinkedBlockingQueue<DatagramPacket>();
	}
	
	@Override
	public void run() {
		try {
			socket.connect(remoteAddress);
			
			socket.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void foward(DatagramPacket packet)
	{
		packets.add(packet);
		packets.notify();
	}
	
	DatagramPacket receive() throws InterruptedException, SocketException
	{
		if (packets.isEmpty())
			packets.wait(remote.getSoTimeout());
		return packets.poll();
	}

}

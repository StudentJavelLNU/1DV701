package ml224ec_assign1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * 
 * @author Martin Lyrå
 *
 */
public class TCPConnectionHandler extends NetLayer implements Runnable {
	private final Socket remote;
	
	TCPConnectionHandler(Socket remote, int bufferSize)
	{
		super(remote.getLocalPort(), bufferSize, 0);
		this.remote = remote;
	}
	
	@Override
	public void run() 
	{
		String msg = "";
		try {
			System.out.printf("TCP connection accepted from %s:%d\n", remote.getInetAddress().getHostAddress(), remote.getPort());
			start();
			msg = "Closed";
		} catch (IOException e) {
			msg = "Socket closed by remote";
		}
		System.out.printf("TCP connection with %s:%d closed. (%s)\n", remote.getInetAddress().getHostAddress(), remote.getPort(), msg);
	}

	@Override
	protected void start() throws IOException 
	{
		DataInputStream in = new DataInputStream(remote.getInputStream());
		DataOutputStream out = new DataOutputStream(remote.getOutputStream());
		
		String receivedMessage = ""; 
		do
		{
			String receivedPart;
			StringBuilder stringBuilder = new StringBuilder();
			do {
				int readBytes = in.read(buffer, 0, bufferSize);
				if (readBytes < 1)
					break;
				receivedPart = new String(buffer, 0, readBytes);
				stringBuilder.append(receivedPart);
			} while (in.available() > 0);
				
			receivedMessage = stringBuilder.toString();
			if (receivedMessage.length() < 1)
				break;
			
			out.write(receivedMessage.getBytes());
			
			System.out.printf("TCP Echo: %d bytes received from %s:%d (User %d)\n",
					receivedMessage.length(), remote.getInetAddress().getHostAddress(), remote.getPort(),
					remote.hashCode());
		} while (!receivedMessage.isEmpty());
		
		remote.close();
	}

	/* Never used */
	@Override
	protected void tick() throws IOException {
		return;
	}
}

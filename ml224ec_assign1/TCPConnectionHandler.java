package ml224ec_assign1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * A special NetLayer implementation for servers that handle connections as new threads.
 * Lifetime is expected to last while the assigned socket is open and remains connected.
 * @author Martin Lyrå
 *
 */
public class TCPConnectionHandler extends NetLayer implements Runnable {
	private final Socket remote;
	
	/**
	 * Takes a connected (accepted and handshaked) socket, this cannot be changed later.
	 * @param remote
	 * @param bufferSize
	 */
	TCPConnectionHandler(Socket remote, int bufferSize)
	{
		super(remote.getLocalPort(), bufferSize, 0);
		this.remote = remote;
	}
	
	/**
	 * Signature provided by Runnable; required to start handling the assigned connection.
	 */
	@Override
	public void run() 
	{
		String msg = "";
		try {
			System.out.printf("TCP connection accepted from %s:%d\n", remote.getInetAddress().getHostAddress(), remote.getPort());
			start(); // Actual work starts here
			msg = "Closed";
		} catch (IOException e) {
			msg = "Socket closed by remote";
		}
		System.out.printf("TCP connection with %s:%d closed. (%s)\n", remote.getInetAddress().getHostAddress(), remote.getPort(), msg);
	}

	@Override
	protected void start() throws IOException 
	{
		// Prepare data streams for fundamental net I/O
		DataInputStream in = new DataInputStream(remote.getInputStream());
		DataOutputStream out = new DataOutputStream(remote.getOutputStream());
		
		String receivedMessage = ""; 
		do
		{
			// Read
			String receivedPart;
			StringBuilder stringBuilder = new StringBuilder();
			do {
				int readBytes = in.read(buffer, 0, bufferSize); // Read, number of bytes read assigned to readBytes
				if (readBytes < 1) // The read function usually does not exit unless new data has been received or when remote socket is closed, the latter will be -1 when it happens
					break;
				receivedPart = new String(buffer, 0, readBytes); // Create part from buffer
				stringBuilder.append(receivedPart); // Add part to builder
			} while (in.available() > 0); // While there is more to read, but sometimes there is always something to be read despite there is nothing to be read, Java please.
			// End Read	
			
			// Assemble parts into one string, if the message is empty, the remote socket has disconnected
			receivedMessage = stringBuilder.toString();
			if (receivedMessage.length() < 1)
				break;
			
			// Reply back to remote
			out.write(receivedMessage.getBytes());
			
			// Print output
			System.out.printf("TCP Echo: %d bytes received from %s:%d (User %d)\n",
					receivedMessage.length(), remote.getInetAddress().getHostAddress(), remote.getPort(),
					remote.hashCode());
		} while (!receivedMessage.isEmpty());
		
		// Clean resource usage by closing socket
		remote.close();
	}

	/* Never used */
	@Override
	protected void tick() throws IOException {
		return;
	}
}

package ml224ec_assign1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * 
 * @author Martin Lyrå
 *
 */
public abstract class NetLayer {
	
	protected static final int UNSIGNED_SHORT_MAX_VAL = Short.MAX_VALUE*2+1;
	
	SocketAddress localAddress;
	SocketAddress remoteAddress;
	
	protected int localPort;
	
	protected int bufferSize;
	protected int transferRate;
	
	protected byte[] buffer;
	
	protected int packetCount;
	
	public NetLayer(int localPort, int bufferSize, int transferRate)
	{	
		
		// Validate input arguments
		if (localPort > UNSIGNED_SHORT_MAX_VAL || localPort < 0)
			throw new IllegalArgumentException(
					String.format("localport: Expected an value between 0 and %d; got %d", Short.MAX_VALUE*2, localPort));
		if (bufferSize < 1)
			throw new IllegalArgumentException(
					String.format("bufferSize: Expected a positive non-zero integer, got %d", bufferSize));
		if (transferRate < 0)
			throw new IllegalArgumentException(
					String.format("transferRate: Expected a positive integer, got %d", transferRate));
		
		// If all input passed validation continue as usual
		this.localPort = localPort;
		this.bufferSize = bufferSize;
		this.transferRate = transferRate;
		
		try {
			buffer = new byte[this.bufferSize];
		} catch (Exception e)
		{
			throw new IllegalArgumentException(
					String.format("bufferSize: An out of memory exception was caused excessive buffer size, try lowering it"));
		}
		
		packetCount = 0;
		
		localAddress = new InetSocketAddress(localPort);
		if (localPort == 0)
			System.out.printf("Value 0 provided as local port; JVM will pick a new port\n");
	}
	
	public NetLayer(String remoteAddress, int remotePort, int localPort, int bufferSize, int transferRate)
	{
		this(localPort, bufferSize, transferRate);
		
		// Validate input
		if (remoteAddress != null && !validateIpv4Address(remoteAddress))
			throw new IllegalArgumentException(
					String.format("remoteAddress: %s is not a valid IPv4 address", remoteAddress));
		if (remotePort < 1 || remotePort > UNSIGNED_SHORT_MAX_VAL)
			throw new IllegalArgumentException(
					String.format("remotePort: Expected an value between 1 and %d; got %d", UNSIGNED_SHORT_MAX_VAL, localPort));
		
		// Reach to here if all checks were passed
		if (remoteAddress == null) // use local
			this.remoteAddress = new InetSocketAddress(remotePort);
		else
			this.remoteAddress = new InetSocketAddress(remoteAddress, remotePort);
	}
	
	/**
	 * Takes an input string and validates if it is an IPv4 address. 
	 * Returns true if it is a correct IPv4 address, otherwise false.
	 * @param address
	 * @return
	 */
	public boolean validateIpv4Address(String address)
	{
		try {
			String[] parts = address.split("\\.");
			if (parts.length != 4)
				return false;
			for (String str : parts)
			{
				int b = Integer.parseInt(str);
				if (b > 255 || b < 0)
					return false;
			}
		} catch (Exception e)
		{
			return false;
		}
		return true;
	}
	
	/**
	 * Starts the client or server, initialization and shutdown are handled here
	 * @throws IOException
	 */
	protected abstract void start() throws IOException;
	
	/**
	 * A work cycle for client, delayed by delay() in runForASecond() function
	 * @throws IOException
	 */
	protected abstract void tick() throws IOException;
	
	/**
	 * Run work() to run function tick() with delay() forever, 
	 * otherwise only once when transferRate = 0
	 * @throws IOException
	 */
	protected void run() throws IOException
	{
		int count = 0;
		long tp = System.currentTimeMillis();
		boolean stop = false;
		while (!stop)
		{
			if (System.currentTimeMillis() - tp < 1000){
				tp = System.currentTimeMillis();
				count = 0;
			}
			if (count <= transferRate) {
				stop = !work();
				count++;
			}
		}
	}
	
	/**
	 * Basic helper function to run work(), running function tick() repeatedly for one second,
	 * delayed with delay() between each loop
	 * @throws IOException
	 */
	protected void runForASecond() throws IOException
	{
		long tp = System.currentTimeMillis();
		while (
				System.currentTimeMillis() - tp < 1000 &&
				packetCount < transferRate &&
				work()
				) ;
		System.out.printf("%d out of %d packets sent and recieved. (%d remaining)\n", packetCount, transferRate, transferRate - packetCount);
	}
	
	/**
	 * The actual work function
	 * @return
	 * @throws IOException
	 */
	private boolean work() throws IOException
	{
		tick();
		packetCount++;
		if (transferRate == 0)
			return false;
		delay();
		return true;
	}
	
	/**
	 * Delay function that sleeps the thread with (1000 / transferRate) milliseconds
	 */
	protected void delay()
	{
		try {
			Thread.sleep(1000/transferRate);
		} catch (InterruptedException e) {
			return;
		}
	}
	
}

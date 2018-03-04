package ml224ec_assign3.tftp;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class TFTPPacket {

	/* Constant(s) & Fields as defined by RFC1350 */
	private static final int DEFAULT_DATA_BUFFER_SIZE = 512;
	
	private final Operation opCode;
	private final short secondCode;
	private byte[] data;
	
	/* Internal only */
	private final int transferId;
	private InetSocketAddress sourceAddress;
	private int dataLength;
	
	public TFTPPacket(Operation opCode, short secondCode, int transferId)
	{
		this(opCode, secondCode, transferId, null);
	}
	
	public TFTPPacket(Operation opCode, short secondCode, int transferId, byte[] data)
	{
		this.opCode = opCode;
		this.secondCode = secondCode;
		this.transferId = transferId;
		
		setData(data);
	}
	
	public static TFTPPacket fromDatagram(DatagramPacket source)
	{
		ByteBuffer buffer = ByteBuffer.wrap(source.getData());
		int dataLength = source.getLength() - 4;
		int transferId = source.getPort();
		
		Operation opCode = Operation.valueOf(buffer.getShort());
		short secondCode = buffer.getShort();
		
		byte[] data = new byte[dataLength];
		buffer.get(data, 0, dataLength);
		
		TFTPPacket packet = new TFTPPacket(opCode, secondCode, transferId, data);
		packet.sourceAddress = (InetSocketAddress) source.getSocketAddress();
		
		return packet;
	}
	
	public Operation getOperationCode()
	{
		return opCode;
	}
	
	public short getBlockId()
	{
		return secondCode;
	}
	
	public Error getErrorCode()
	{
		if (opCode == Operation.ERROR)
			return Error.valueOf(secondCode);
		return null;
	}
	
	public int getTransferId()
	{
		return transferId;
	}
	
	public InetSocketAddress getSourceAddress()
	{
		return sourceAddress;
	}
	
	public byte[] getData()
	{
		byte[] data = new byte[dataLength];
		System.arraycopy(this.data, 0, data, 0, dataLength);
		return data;
	}
	
	public byte[] getDataBuffer()
	{
		return data;
	}
	
	public void setData(byte[] data)
	{
		if (data != null)
		{
			this.data = new byte[data.length];
			System.arraycopy(data, 0, this.data, 0, data.length);
			dataLength = data.length;
		}
		else
		{
			this.data = new byte[0];
			dataLength = 0;
		}
	}
	
	public DatagramPacket toDatagram()
	{
		int length = 4 + dataLength;
		DatagramPacket packet = new DatagramPacket(new byte[length], length);
		ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
		byte[] data = getData();
		
		buffer.putShort(opCode.getCode());
		buffer.putShort(secondCode);
		buffer.put(data, 0, data.length);
		
		return packet;
	}
}

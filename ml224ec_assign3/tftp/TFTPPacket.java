package ml224ec_assign3.tftp;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Object to represent TFTP packets as specified by RFC1350
 * @author Martin Lyrå
 *
 */
public class TFTPPacket {

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
	
	/**
	 * Manufactures a TFTPPacket from the contents of a given DatagramPacket
	 * @param source
	 * @return
	 */
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
	
	/**
	 * Returns the operation code specified for this packet and its data
	 * @return
	 */
	public Operation getOperationCode()
	{
		return opCode;
	}
	
	/**
	 * Returns the second short code in the packet, after the opcode
	 * @return
	 */
	public short getBlockId()
	{
		return secondCode;
	}
	
	/**
	 * If the packet is an error packet, it returns a code, otherwise null
	 * @return
	 */
	public Error getErrorCode()
	{
		if (opCode == Operation.ERROR)
			return Error.valueOf(secondCode);
		return null;
	}
	
	/**
	 * Transfer ID from packet's source
	 * @return
	 */
	public int getTransferId()
	{
		return transferId;
	}
	
	/**
	 * Source address this packet originates from
	 * @return
	 */
	public InetSocketAddress getSourceAddress()
	{
		return sourceAddress;
	}
	
	/**
	 * Returns data for this object from the buffer
	 * @return
	 */
	public byte[] getData()
	{
		byte[] data = new byte[dataLength];
		System.arraycopy(this.data, 0, data, 0, dataLength);
		return data;
	}
	
	/**
	 * Returns the entire data buffer for this packet object
	 * @return
	 */
	public byte[] getDataBuffer()
	{
		return data;
	}
	
	/**
	 * Sets the data bytes for this packet
	 * @param data
	 */
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
	
	/**
	 * Turns the TFTP packet into an UDP packet for
	 * transmission via UDP
	 * @return
	 */
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

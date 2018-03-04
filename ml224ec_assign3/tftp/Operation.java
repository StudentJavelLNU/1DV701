package ml224ec_assign3.tftp;

/**
 * Enum for specifying and representing operation codes in TFTP
 * as specified by RFC1350
 * @author Martin Lyrå
 *
 */
public enum Operation {
	UNDEFINED(0),
	
	READ_REQUEST(1),
	WRITE_REQUEST(2),
	DATA(3),
	ACKNOWLEDGE(4),
	ERROR(5);
	
	Operation(int val)
	{
		value = (short) val;
	}
	
	public short getCode()
	{
		return value;
	}
	
	public static  Operation valueOf(short val)
	{
		for (Operation op : values())
			if (op.value == val)
				return op;
		return UNDEFINED;
	}
	
	private final short value;
}

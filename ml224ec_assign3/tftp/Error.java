package ml224ec_assign3.tftp;

/**
 * Enum for specifying and representing error codes in TFTP as specified
 * by RFC1350
 * @author Martin Lyrå
 *
 */
public enum Error {
	UNKNOWN(0), // undefined or other
	FILE_NOT_FOUND(1),
	ACCEESS_VIOLATION(2),
	OUT_OF_DISK_SPACE(3),
	ILLEGAL_OPERATION(4),
	UNKNOWN_TRANSFER_ID(5),
	FILE_ALREADY_EXISTS(6),
	NO_SUCH_USER(7);
	
	private final short value;
	
	Error(int val)
	{
		value = (short) val;
	}
	
	public short getCode()
	{
		return value;
	}

	public static Error valueOf(short val) {
		for (Error err : values())
			if (err.value == val)
				return err;
		return UNKNOWN;
	}
}

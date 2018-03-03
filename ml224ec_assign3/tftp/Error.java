package ml224ec_assign3.tftp;

public enum Error {
	UNKNOWN(0), // undefined or other
	FILE_NOT_FOUND(1),
	ACCEESS_VIOLATION(2),
	OUT_OF_DISK_SPACE(3),
	ILLEGAL_OPERATION(4),
	UNKNOWN_TRANSFER_ID(5),
	FILE_ALREADY_EXISTS(6),
	NO_SUCH_USER(7);
	
	Error(int val)
	{
		errorCode = (short) val;
	}
	
	public short getCode()
	{
		return errorCode;
	}
	
	private final short errorCode;
}

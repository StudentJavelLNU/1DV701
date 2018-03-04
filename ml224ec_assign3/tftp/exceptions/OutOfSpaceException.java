package ml224ec_assign3.tftp.exceptions;

import ml224ec_assign3.tftp.Error;

/**
 * Represents TFTP error #3
 * @author Martin Lyrå
 *
 */
public class OutOfSpaceException extends TFTPException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7068493137675608072L;
	
	public OutOfSpaceException(String message)
	{
		super(message, Error.OUT_OF_DISK_SPACE);
	}
	
	public OutOfSpaceException()
	{
		this("Out of Disk Space");
	}
}

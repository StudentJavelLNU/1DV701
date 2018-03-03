package ml224ec_assign3.tftp.exceptions;

import ml224ec_assign3.tftp.Error;

public class TFTPException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8809898569349419109L;
	
	protected Error associatedError;
	
	protected TFTPException(String message, Error error)
	{
		super(message);
		associatedError = error;
	}
	
	public TFTPException(String message)
	{
		this(message, Error.UNKNOWN);
	}
	
	public TFTPException() {
		this("Unknown error");
	}
	
	public Error getAssociatedErrorCode()
	{
		return associatedError;
	}
}

package ml224ec_assign3.tftp.exceptions;

import ml224ec_assign3.tftp.Error;

public class IllegalOperationException extends TFTPException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8067328435343969418L;

	public IllegalOperationException(String message) {
		super(message);
		associatedError = Error.ILLEGAL_OPERATION;
	}

	public IllegalOperationException() {
		this("Illegal operation");
	}
}

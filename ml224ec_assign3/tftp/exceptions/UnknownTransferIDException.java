package ml224ec_assign3.tftp.exceptions;

import ml224ec_assign3.tftp.Error;

public class UnknownTransferIDException extends TFTPException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3019158630293271376L;
	
	public UnknownTransferIDException(String message) {
		super(message);
		associatedError = Error.UNKNOWN_TRANSFER_ID;
	}
}

package ml224ec_assign3.tftp.exceptions;

import ml224ec_assign3.tftp.Error;

public class AccessViolationException extends TFTPException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5079793907878336657L;

	public AccessViolationException()
	{
		super("Access Violation", Error.ACCEESS_VIOLATION);
	}
}

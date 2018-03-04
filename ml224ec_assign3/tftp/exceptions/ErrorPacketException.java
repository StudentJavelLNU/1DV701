package ml224ec_assign3.tftp.exceptions;

import ml224ec_assign3.tftp.Error;

public class ErrorPacketException extends TFTPException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 939031133151439727L;

	public ErrorPacketException()
	{
		super("ERR received from client;s connection closed by client", Error.UNKNOWN);
	}
}

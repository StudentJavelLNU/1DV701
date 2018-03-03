package ml224ec_assign3.tftp.exceptions;

/**
 * Non-standard exception for when a connection times out between two sockets
 * @author Martin Lyrå
 *
 */
public class TimeoutException extends TFTPException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8864288215778191589L;

	public TimeoutException()
	{
		super("Connection timeout");
	}
}

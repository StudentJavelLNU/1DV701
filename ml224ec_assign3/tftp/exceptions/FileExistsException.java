package ml224ec_assign3.tftp.exceptions;

import ml224ec_assign3.tftp.Error;

/**
 * Represents TFTP error #6
 * @author Martin Lyrå
 *
 */
public class FileExistsException extends TFTPException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4070282354173734525L;

	public FileExistsException(String message) {
		super(message);
		associatedError = Error.FILE_ALREADY_EXISTS;
	}

	public FileExistsException() {
		this("File already exists");
	}
}

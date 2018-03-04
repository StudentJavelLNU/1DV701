package ml224ec_assign3.tftp.exceptions;

import ml224ec_assign3.tftp.Error;

/**
 * Represents TFTP error #1
 * @author Martin Lyrå
 *
 */
public class FileNotFoundException extends TFTPException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5639861545979747758L;

	public FileNotFoundException(String message) {
		super(message);
		associatedError = Error.FILE_NOT_FOUND;
	}

	public FileNotFoundException() {
		this("File not found");
	}
}

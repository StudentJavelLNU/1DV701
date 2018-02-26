package ml224ec_assign2.exceptions;

/**
 * An exception meant to represent the HTTP user error; 400 - Bad Request
 * @author Martin Lyrå
 *
 */
public class HttpBadRequestException extends Exception {

	public HttpBadRequestException(String string) {
		super(string);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5302334093902096629L;

}

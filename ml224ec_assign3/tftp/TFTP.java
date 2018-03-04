package ml224ec_assign3.tftp;

/**
 * Static object to specify essential constants specified
 * by RFC1350
 * @author Martin Lyrå
 *
 */
public final class TFTP {
	public static final int DATA_BUFFER_SIZE = 512;
	public static final int HEAD_SIZE = 4;
	public static final int PACKET_BUFFER_SIZE = DATA_BUFFER_SIZE + HEAD_SIZE;
}

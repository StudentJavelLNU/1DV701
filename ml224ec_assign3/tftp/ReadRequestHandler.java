package ml224ec_assign3.tftp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ml224ec_assign3.TFTPServer;
import ml224ec_assign3.tftp.exceptions.AccessViolationException;
import ml224ec_assign3.tftp.exceptions.FileNotFoundException;
import ml224ec_assign3.tftp.exceptions.IllegalOperationException;
import ml224ec_assign3.tftp.exceptions.TFTPException;

public class ReadRequestHandler extends AbstractRequestHandler {

	private int maxBlocks;
	
	public ReadRequestHandler(String targetFile) {
		super(targetFile);
	}

	@Override
	protected void preStartCheck() throws TFTPException {
		Path path = Paths.get(targetFile);
		
		if (!Files.exists(path))
			throw new FileNotFoundException();
		
		try {
			buffer = ByteBuffer.wrap(Files.readAllBytes(path));
		} catch (SecurityException e) {
			throw new AccessViolationException();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new TFTPException(e.getMessage());
		}
	}
	
	@Override
	protected void handleRequest(DatagramSocket socket, TFTPPacket packet, boolean firstTime) throws TFTPException, IOException {
		int acknowledgedBlock = 0;
		if (!firstTime)
		{
			if (packet.getOperationCode() != Operation.ACKNOWLEDGE)
				throw new IllegalOperationException();
			acknowledgedBlock = packet.getBlockId();
		}
		
		if (!transferDone) {
			currentBlock++;
			TFTPPacket sendPacket = new TFTPPacket(Operation.DATA, currentBlock, socket.getLocalPort());
			
			int offset = buffer.position();
			int length = TFTP.DATA_BUFFER_SIZE;
			int remainingLength = buffer.remaining();
			
			
			if(remainingLength < length)
			{
				length = remainingLength;
			}
			
			if (TFTPServer.DEBUG) {
				System.out.printf("#%d: send %d bytes of data\n",currentBlock, length);
				System.out.printf("#%d: position %d out of %d (%d remaining)\n",
						currentBlock, offset, buffer.limit(), remainingLength);
			}
			
			if (length > 0)
			{
				byte[] data = new byte[length];
				buffer.get(data, 0, length);
				
				sendPacket.setData(data);
			}
			
			socket.send(sendPacket.toDatagram());
			lastPacket = sendPacket;
			packetsSent++;
			
			if (isLastBlock(currentBlock))
				transferDone = true;
		}
		synced = currentBlock == acknowledgedBlock;
	}

	@Override
	public Operation requestTypeHandled() {
		return Operation.READ_REQUEST;
	}

	private boolean isLastBlock(int block)
	{
		/* Calculate the limit when it is uninitialized */
		if (maxBlocks < 1) {
			double a = buffer.limit();
			double b = TFTP.DATA_BUFFER_SIZE;
			double f1 = (a/b);
			double f2 = (b - (a%b))/b;
	
			maxBlocks = (int) (Math.ceil(f1) + Math.floor(f2));
		}
		/* Answer the question, is the given block number the last? */
		return block >= maxBlocks;
	}
}

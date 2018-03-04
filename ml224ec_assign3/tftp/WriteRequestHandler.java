package ml224ec_assign3.tftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ml224ec_assign3.TFTPServer;
import ml224ec_assign3.tftp.exceptions.AccessViolationException;
import ml224ec_assign3.tftp.exceptions.FileExistsException;
import ml224ec_assign3.tftp.exceptions.IllegalOperationException;
import ml224ec_assign3.tftp.exceptions.OutOfSpaceException;
import ml224ec_assign3.tftp.exceptions.TFTPException;

public class WriteRequestHandler extends AbstractRequestHandler {

	public WriteRequestHandler(String targetFile) {
		super(targetFile);
	}

	@Override
	public Operation requestTypeHandled() {
		return Operation.WRITE_REQUEST;
	}

	@Override
	protected void handleRequest(DatagramSocket socket, TFTPPacket packet, boolean firstTime) 
			throws TFTPException, IOException 
	{
		if(!firstTime)
		{
			if (packet.getOperationCode() != Operation.DATA)
				throw new IllegalOperationException();
			
			/* Get data */
			currentBlock = packet.getBlockId();
			byte[] data = packet.getData();
			
			/* Append data to file */
			writeToFile(targetFile, data);
			
			transferDone = data.length < TFTP.DATA_BUFFER_SIZE;
			
			if (TFTPServer.DEBUG)
				System.out.printf("#%d: %d bytes received%s\n", currentBlock, data.length,
						transferDone ? ", end of transfer" : "");
		}
		
		/* Send acknowledgement */
		int tid = socket.getLocalPort();
		TFTPPacket sendPacket = new TFTPPacket(Operation.ACKNOWLEDGE, currentBlock, tid);
		
		send(socket, sendPacket);
		
		if (TFTPServer.DEBUG)
			System.out.printf("#%d: Acknowledging\n", currentBlock);
		
		synced = transferDone;
	}

	@Override
	protected void preStartCheck() throws TFTPException {
		Path path = Paths.get(targetFile);
		
		if (Files.exists(path) && !TFTPServer.OVERWRITE)
			throw new FileExistsException();
		
		/* Try to create the file */
		try {
			if (TFTPServer.OVERWRITE)
				Files.deleteIfExists(path);
			Files.createFile(path);
		} 
		/* Catch exceptions from above attempt */
		catch (SecurityException e) 
		{
			throw new AccessViolationException();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new TFTPException(e.getMessage());
		}
	}

	/**
	 * Append [all] bytes to destination file
	 * @param file
	 * @param data
	 * @throws IOException
	 * @throws TFTPException
	 */
	private void writeToFile(String file, byte[] data) 
			throws IOException, TFTPException
	{
		if (getFreeDiskSpaceBytes() < data.length)
			throw new OutOfSpaceException();
		try (FileOutputStream os = new FileOutputStream(file, true))
		{
			os.write(data);
		} 
	}
	
	private long getFreeDiskSpaceBytes()
	{
		return new File(targetFile.substring(0, targetFile.lastIndexOf('/'))).getFreeSpace();
	}
}

package ml224ec_assign2;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import ml224ec_assign2.exceptions.HttpBadRequestException;

public class ImageUploadService {
	public static HttpResponse completeRequest(HttpRequest request)
	{
		HttpResponse response = null;
		try {
			int contentLength = Integer.parseInt(request.getField("Content-Length"));
			if (contentLength > 0)
			{
				HttpContent imageContent = request.getContentByName("Image 1");
				
				if (imageContent == null)
					throw new HttpBadRequestException("Expected an image in POST, got none");
				
				String filename = HttpParser.getAttribute("filename", imageContent.getDisposition())
						.replaceAll("\"", "");
				
				if (filename.isEmpty())
					throw new HttpBadRequestException("Expected an image in request, got an empty block");
				
				String relativeDest = "/uploaded/" + filename;
				Path dest = Paths.get(
						WebServer.CONTENT_PATH + relativeDest);
				
				boolean targetExists = dest.toFile().exists();
				if( !targetExists || request.getMethod().equals("PUT") )
				{
					FileOutputStream fos = new FileOutputStream(dest.toString());
					fos.write(imageContent.getContentData());
					fos.flush();
					fos.close();
					
					if (targetExists)
						System.out.printf("File %s has been updated by user.\n", filename);
					else 
						System.out.printf("File %s uploaded to server.\n", filename);
				} else
					System.out.printf("File %s already exists, use PUT to update file\n",
							filename);
				
				response = new HttpResponse(302); // Found (redirect)
				response.setRedirectLocation(relativeDest);
			}
		} catch (IOException e)
		{
			response = new HttpResponse(500); // Method not Supported
		}
		catch (HttpBadRequestException e)
		{
			response = new HttpResponse(304); // Not Modified
		}
		
		return response;
	}
}

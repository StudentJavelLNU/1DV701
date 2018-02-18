package ml224ec_assign2;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpContent {
	private final String disposition;
	private final String type;
	
	private final byte[] data;
	
	HttpContent(String contentString) throws UnsupportedEncodingException
	{
		this(HttpParser.parse(contentString));
	}
	
	HttpContent(Map<String, String> contentData) throws UnsupportedEncodingException
	{	

		disposition = contentData.get("Content-Disposition");
		type = contentData.get("Content-Type").trim();
		data = contentData.get("Content-Data").trim().getBytes("ISO-8859-15");
	}
	
	/*
	private static Map<String, String> parseList(List<String> list)
	{
		Map<String, String> fields = new HashMap<String, String>();
		
		boolean endOfHeader = false;
		String contentDataString = "";
		for (int i = 0; i < list.size(); i++)
		{
			String part = list.get(i);
			if (part.isEmpty())
				endOfHeader = true;
			else if (!endOfHeader)
			{
				int si = part.indexOf(':');
				if (si > -1)
					fields.put(part.substring(0, si), part.substring(si+1).trim());
			}
			else
				contentDataString += String.format("%s\n", part);
		}
		fields.put("Content-Data", contentDataString);
		
		return fields;
	}*/
	
	public String getDisposition()
	{
		return disposition;
	}
	
	public String getType()
	{
		return type;
	}
	
	public byte[] getContentData()
	{
		return data;
	}
	
	public String toString()
	{
		return String.format("HttpContent-%d\nContent-Disposition: %s\nContent-Type: %s\nContent-Data: byte[%d]",
				this.hashCode(),
				disposition,
				type,
				data.length);
	}
}

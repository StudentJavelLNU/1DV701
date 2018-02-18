package ml224ec_assign2;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class HttpContent extends HttpBase {
	HttpContent(String contentString)
	{
		this(HttpParser.parse(contentString, true));
	}
	
	HttpContent(Map<String, String> contentData)
	{	
		fields = contentData;
	}
	
	public String getDisposition()
	{
		return getField("Content-Disposition");
	}
	
	public String getType()
	{
		return getField("Content-Type");
	}
	
	public byte[] getContentData() throws UnsupportedEncodingException
	{
		return HttpParser.getArbitaryData(getField("Content-Data").trim());
	}
	
	public String toString()
	{
		try {
			return String.format("HttpContent-%d\nContent-Disposition: %s\nContent-Type: %s\nContent-Data: byte[%d]",
					this.hashCode(),
					getDisposition(),
					getType(),
					getContentData().length);
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}
}

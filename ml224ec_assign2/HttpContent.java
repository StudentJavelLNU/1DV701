package ml224ec_assign2;

import java.util.Map;

/**
 * A class designed to wrap up content data in the body of HTTP requests
 * into more accessible objects, like HttpRequest and HttpResponse.
 * @author Martin Lyrå
 *
 */
public class HttpContent extends HttpBase {
	HttpContent(String contentString)
	{
		this(HttpParser.parse(contentString, true));
	}
	
	HttpContent(Map<String, String> contentData)
	{	
		fields = contentData;
	}
	
	/**
	 * Returns the value for of the "Content-Disposition" field
	 * @return
	 */
	public String getDisposition()
	{
		return getField("Content-Disposition");
	}
	
	/**
	 * Returns the value the media type of content. Returns null
	 * if the "Content-Type" field does not exist.
	 * @return
	 */
	public String getType()
	{
		return getField("Content-Type");
	}
	
	/**
	 * Returns the body of HttpContent, the returned type is an byte array of arbitrary data.
	 * @return
	 */
	public byte[] getContentData()
	{
		return HttpParser.getArbitraryData(getField("Content-Data").trim());
	}
	
	/**
	 * Returns the contents of the HTTP content as a single string
	 */
	public String toString()
	{
		return String.format("HttpContent-%d\nContent-Disposition: %s\nContent-Type: %s\nContent-Data: byte[%d]",
				this.hashCode(),
				getDisposition(),
				getType(),
				getContentData().length);
	}
}

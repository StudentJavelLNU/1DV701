package ml224ec_assign2;

public enum HttpStatusCode {
	OK(200, "OK"),
	
	FOUND(302, "Found"),
	CREATED(303, "Created"),
	NOT_MODIFIED(304, "Not Modified"),
	
	BAD_REQUEST(400, "Bad Request"),
	FORBIDDEN(403, "Forbidden"),
	NOT_FOUND(404, "Not Found"),
	
	INTERNAL_ERROR(500, "Internal Error"),
	METHOD_NOT_SUPPORTED(502, "Method not Supported"),
	
	/* Unofficial codes */
	UNKNOWN_ERROR(520, "Unknown Error"); // Based off Cloudflare's definition of 520 error
	
	private final int code;
	private final String msg;
	
	HttpStatusCode(int code, String msg)
	{
		this.code = code;
		this.msg = msg;
	}
	
	public int getCode()
	{
		return code;
	}
	
	public String getMessage()
	{
		return msg;
	}
	
	public static HttpStatusCode valueOf(int integer)
	{
		for (HttpStatusCode c : HttpStatusCode.values())
			if (c.getCode() == integer)
				return c;
		return UNKNOWN_ERROR;
	}
	
	public String toString()
	{
		return String.format("%d: %s", code, msg);
	}
}

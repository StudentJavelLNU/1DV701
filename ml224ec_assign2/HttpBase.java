package ml224ec_assign2;

import java.util.Map;
import java.util.HashMap;

public abstract class HttpBase {
	
	protected static final String CRLF = "\r\n";
	
	protected Map<String, String> fields = 
			new HashMap<String, String>();
	
	public String setField(String fieldName, String fieldData)
	{
		return fields.put(fieldName, fieldData);
	}
	
	public boolean hasField(String fieldName)
	{
		return fields.get(fieldName) != null;
	}
	
	public String getField(String fieldName)
	{
		return fields.get(fieldName);
	}
	
	public String getFieldString(String fieldName)
	{
		return String.format("%s: %s", fieldName, fields.get(fieldName));
	}
}

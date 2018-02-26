package ml224ec_assign2;

import java.util.Map;
import java.util.HashMap;

/**
 * Abstract base object for wrapping HTTP standard as accessible objects with methods for manipulating them.
 * @author Martin Lyrå
 *
 */
public abstract class HttpBase {
	
	protected static final String CRLF = "\r\n";
	
	/**
	 * Internal data fields to store and keep data by associated field name
	 */
	protected Map<String, String> fields = 
			new HashMap<String, String>();
	
	/**
	 * Creates or sets a HTTP field with given value for given field by name.
	 * @param fieldName
	 * @param fieldData
	 * @return
	 */
	public String setField(String fieldName, String fieldData)
	{
		return fields.put(fieldName, fieldData);
	}
	
	/**
	 * Returns true if this HTTP field exists
	 * @param fieldName
	 * @return
	 */
	public boolean hasField(String fieldName)
	{
		return fields.get(fieldName) != null;
	}
	
	/**
	 * Returns the value for HTTP field given by name.
	 * @param fieldName
	 * @return
	 */
	public String getField(String fieldName)
	{
		return fields.get(fieldName);
	}
	
	/**
	 * Returns the entire line, including name and value, for the given HTTP field by name.
	 * @param fieldName
	 * @return
	 */
	public String getFieldString(String fieldName)
	{
		return String.format("%s: %s", fieldName, fields.get(fieldName));
	}
}

package tools;

/**
 * @author Sebastian Kraemer
 * @created 11.09.2004
 * 
 * class for an listitem to have an element that support a key0 and value pair
 */

public class ListItem {

	private String key0;
	private String key1;
	private String value;
	
	/**
	 * @param key0 the key0 of this object
	 * @param value the value of this object
	 */
	public ListItem(String key0, String value) {
		this.key0   = key0;
		this.key1   = "";
		this.value  = value;
	}
	
	/**
	 * @param key0 the key0 of this object
	 * @param value the value of this object
	 */
	public ListItem(String key0, String key1, String value) {
		this.key0   = key0;
		this.key1   = key1;
		this.value  = value;
	}
	
	public String toString(){
		return value;
	}
	
	/**
	 * @return Returns the key0.
	 */
	public String getKey0() {
		return key0;
	}
	
	/**
	 * @param key0 The key0 to set.
	 */
	public void setKey0(String key) {
		this.key0 = key;
	}
	
	/**
	 * @return Returns the value.
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * @param value The value to set.
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	public void printMe(){
		System.out.println(this.key0+"|"+this.key1+"|"+this.value);
	}
	
	/**
	 * @return Returns the key1.
	 */
	public String getKey1() {
		return key1;
	}
	
	/**
	 * @param key1 The key1 to set.
	 */
	public void setKey1(String key1) {
		this.key1 = key1;
	}
}

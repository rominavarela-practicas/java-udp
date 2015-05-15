package model;

public abstract class model {
	
	/**
	 * @return XML-formated String wrapping object
	 */
	public abstract String key();
	public abstract String serialize();
	
	public String toString()
	{
		return serialize();
	};
	
	public byte[] toByteArray()
	{	
		return this.serialize().getBytes();
	}
}

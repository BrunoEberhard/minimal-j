package ch.openech.mj.db.model;


/**
 * The description of what means the content of a String
 * 
 * @author Bruno
 *
 */
public interface Format {

	/**
	 * 
	 * @return the class a string "means". e.g. Integer.class for "123" or Boolean.class for "1"
	 * Used to evaluate the type of the db column
	 */
	public Class<?> getClazz();
	
	public int getSize();
	
	/**
	 * 
	 * @param value the value in the object or database
	 * @return the value as it is displayed to the user (in read only mode)
	 */
	public String display(String value);

	/**
	 * 
	 * @param value the value in the object or database
	 * @return the value as it is displayed to the user (for edit, for example numbers without ')
	 */
	public String displayForEdit(String value);

}

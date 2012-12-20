package ch.openech.mj.edit.fields;

import ch.openech.mj.db.model.PropertyInterface;
import ch.openech.mj.toolkit.IComponent;

/**
 * A read only / display Field in a Form.<br>
 * note: A FormField is not a component but provides a component
 *
 * @param <T> The type of the value of this field.
 */
public interface FormField<T> {

	/**
	 * Set the value
	 * 
	 * @param object
	 */
	public void setObject(T object);

	
	public IComponent getComponent();
	
	public PropertyInterface getProperty();

}
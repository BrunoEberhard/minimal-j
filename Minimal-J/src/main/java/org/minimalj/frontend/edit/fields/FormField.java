package org.minimalj.frontend.edit.fields;

import org.minimalj.frontend.toolkit.IComponent;
import org.minimalj.model.PropertyInterface;

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
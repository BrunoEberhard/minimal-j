package org.minimalj.frontend.toolkit;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;


public interface ComboBox<T> extends IComponent {

	/**
	 * if the object is not in the list of objects provided in the constructor
	 * the list must temporary add the object. getSelectedObject must return
	 * an equal object if the user never touches the field
	 * 
	 * @param object may be <code>null</code>
	 */
	public void setSelectedObject(T object);

	public T getSelectedObject();

	public void setEditable(boolean editable);
}

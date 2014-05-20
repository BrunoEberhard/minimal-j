package org.minimalj.frontend.toolkit;

import java.util.List;


public interface ComboBox<T> extends IComponent {

	public void setObjects(List<T> object);
	
	public void setSelectedObject(T object) throws IllegalArgumentException;

	public T getSelectedObject();

}

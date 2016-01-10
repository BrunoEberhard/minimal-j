package org.minimalj.frontend.form.element;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.ChangeListener;

/**
 * A FormElement is not a component but provides a component
 *
 * There are FormElements that can be created for both changeable
 * and readonly mode. They have an 'editable' parameter in the constructor.
 * And there are FormElements that can only be created
 * as changeable elements. Then as read only element is
 * TextFormElement used.
 *
 * @param <T> The type of the value of this field.
 */
public interface FormElement<T> {

	public void setValue(T object);

	public T getValue();

	public void setChangeListener(ChangeListener<FormElement<?>> listener);

	public IComponent getComponent();
	
	public PropertyInterface getProperty();

}
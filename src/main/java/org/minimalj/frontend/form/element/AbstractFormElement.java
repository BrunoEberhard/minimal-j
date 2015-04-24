package org.minimalj.frontend.form.element;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.ClientToolkit.InputComponentListener;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;

public abstract class AbstractFormElement<T> implements FormElement<T> {

	private final PropertyInterface property;
	
	private InputComponentListener forwardingChangeListener;
	private FormElementListener changeListener;

	protected AbstractFormElement(Object key) {
		this(Keys.getProperty(key));
	}
	
	protected AbstractFormElement(PropertyInterface property) {
		if (property == null) throw new IllegalArgumentException();
		
		this.property = property;
	}

	@Override
	public PropertyInterface getProperty() {
		return property;
	}
	
	// Listener
	
	protected InputComponentListener listener() {
		if (forwardingChangeListener == null) {
			forwardingChangeListener = new ForwardingChangeListener();
		}
		return forwardingChangeListener;
	}
	
	@Override
	public void setChangeListener(FormElementListener changeListener) {
		if (changeListener == null) {
			throw new IllegalArgumentException("ChangeListener must not be null");
		}
		if (this.changeListener != null) {
			throw new IllegalStateException("ChangeListener can only be set once");
		}
		this.changeListener = changeListener;
	}
	
	protected void fireChange() {
		if (changeListener != null) {
			changeListener.valueChanged(AbstractFormElement.this);
		}
	}
	
	private class ForwardingChangeListener implements InputComponentListener {
		@Override
		public void changed(IComponent source) {
			fireChange();
		}
	}

}

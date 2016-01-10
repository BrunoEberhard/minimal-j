package org.minimalj.frontend.form.element;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.ChangeListener;

public abstract class AbstractFormElement<T> implements FormElement<T> {

	private final PropertyInterface property;
	
	private InputComponentListener forwardingChangeListener;
	private ChangeListener<FormElement<?>> changeListener;

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
	public void setChangeListener(ChangeListener<FormElement<?>> changeListener) {
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
			changeListener.changed(AbstractFormElement.this);
		}
	}
	
	private class ForwardingChangeListener implements InputComponentListener {
		@Override
		public void changed(IComponent source) {
			fireChange();
		}
	}

}

package org.minimalj.frontend.form.element;

import java.util.Objects;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.Property;
import org.minimalj.util.ChangeListener;
import org.minimalj.util.resources.Resources;

public abstract class AbstractFormElement<T> implements FormElement<T> {

	private final Property property;
	
	private InputComponentListener forwardingChangeListener;
	private ChangeListener<FormElement<?>> changeListener;

	private FormElementConstraint constraint;

	protected AbstractFormElement(Object key) {
		this(Keys.getProperty(key));
	}
	
	protected AbstractFormElement(Property property) {
		this.property = Objects.requireNonNull(property);
	}

	@Override
	public Property getProperty() {
		return property;
	}
	
	@Override
	public String getCaption() {
		return Resources.getPropertyName(getProperty());
	}

	@Override
	public FormElementConstraint getConstraint() {
		return constraint;
	}

	public AbstractFormElement<T> height(int height) {
		constraint = new FormElementConstraint(height, height);
		return this;
	}

	public AbstractFormElement<T> height(int min, int max) {
		constraint = new FormElementConstraint(min, max);
		return this;
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
			throw new IllegalStateException("ChangeListener can only be set once on " + getProperty().toString());
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

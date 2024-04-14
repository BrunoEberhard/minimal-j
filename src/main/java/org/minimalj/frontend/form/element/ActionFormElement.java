package org.minimalj.frontend.form.element;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.action.Action;
import org.minimalj.model.properties.Property;
import org.minimalj.model.properties.VirtualProperty;
import org.minimalj.util.ChangeListener;

public class ActionFormElement implements FormElement<Void> {

	public final IComponent component;
	public final String caption;

	public ActionFormElement(Action action) {
		this(action, null, true);
	}

	public ActionFormElement(Action action, String caption, boolean link) {
		component = Frontend.getInstance().createText(action);
		this.caption = caption;
	}

	@Override
	public void setValue(Void object) {
		// not implemented
	}

	@Override
	public Void getValue() {
		// not implemented
		return null;
	}

	@Override
	public void setChangeListener(ChangeListener<FormElement<?>> listener) {
		// not implemented
	}

	@Override
	public IComponent getComponent() {
		return component;
	}

	@Override
	public Property getProperty() {
		return new DummyProperty();
	}

	private class DummyProperty extends VirtualProperty {

		@Override
		public String getName() {
			return "";
		}

		@Override
		public Class<?> getClazz() {
			return Object.class;
		}

		@Override
		public Object getValue(Object object) {
			return null;
		}

		@Override
		public void setValue(Object object, Object value) {
			//
		}
	}

	@Override
	public FormElementConstraint getConstraint() {
		// not implemented
		return null;
	}

	@Override
	public String getCaption() {
		return caption;
	}

	@Override
	public boolean canBeEmpty() {
		return false;
	}

}
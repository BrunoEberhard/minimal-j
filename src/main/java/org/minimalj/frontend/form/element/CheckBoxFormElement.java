package org.minimalj.frontend.form.element;

import java.util.Objects;
import java.util.Set;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.properties.VirtualProperty;
import org.minimalj.util.resources.Resources;

public class CheckBoxFormElement extends AbstractFormElement<Boolean> {
	private final Input<Boolean> checkBox;
	private final boolean caption;
	
	public CheckBoxFormElement(PropertyInterface property, boolean editable) {
		 this(property, Resources.getPropertyName(property, ".checkBoxText"), editable);
	}
	 
	public CheckBoxFormElement(PropertyInterface property, String text, boolean editable) {
		this(property, text, editable, true);
	}

	public CheckBoxFormElement(PropertyInterface property, String text, boolean editable, boolean caption) {
		super(property);
		this.caption = caption;
		checkBox = Frontend.getInstance().createCheckBox(listener(), text);
		checkBox.setEditable(editable);
	}
	
	@Override
	public String getCaption() {
		return caption ? super.getCaption() : null;
	}

	@Override
	public IComponent getComponent() {
		return checkBox;
	}
	
	@Override
	public Boolean getValue() {
		return checkBox.getValue();
	}		
	
	@Override
	public void setValue(Boolean value) {
		checkBox.setValue(Boolean.TRUE.equals(value));
	}

	public static abstract class CheckBoxProperty extends VirtualProperty {

		@Override
		public Class<?> getDeclaringClass() {
			return Object.class;
		}

		@Override
		public Class<?> getClazz() {
			return Boolean.class;
		}

		@Override
		public String getName() {
			return null; // should not be used
		}
		
		@Override
		public abstract Boolean getValue(Object object);

		@Override
		public abstract void setValue(Object object, Object newValue);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static class SetElementFormElementProperty extends CheckBoxProperty {
		private final Object value;

		public SetElementFormElementProperty(Object value) {
			this.value = Objects.requireNonNull(value);
		}

		@Override
		public Boolean getValue(Object object) {
			Set set = (Set) object;
			return set.contains(value);
		}

		@Override
		public void setValue(Object object, Object newValue) {
			Set set = (Set) object;
			if (Boolean.TRUE.equals(newValue)) {
				set.add(value);
			} else {
				set.remove(value);
			}
		}
	}

}

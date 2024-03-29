package org.minimalj.frontend.form.element;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.form.Form;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.Property;
import org.minimalj.model.properties.VirtualProperty;
import org.minimalj.util.resources.Resources;

public class CheckBoxFormElement extends AbstractFormElement<Boolean> implements Enable {
	private final Input<Boolean> checkBox;
	private final boolean caption;
	private final boolean editable;

	public CheckBoxFormElement(Property property, boolean editable) {
		this(property, getPropertyName(property), editable);
	}

	private static String getPropertyName(Property property) {
		String name = Resources.getPropertyName(property, "checkBoxText");
		if (name == null) {
			name = Resources.getPropertyName(property);
		}
		return name;
	}

	public CheckBoxFormElement(Property property, String text, boolean editable) {
		this(property, text, editable, true);
	}

	public CheckBoxFormElement(Property property, String text, boolean editable, boolean caption) {
		super(property);
		this.editable = editable;
		this.caption = caption;
		checkBox = Frontend.getInstance().createCheckBox(listener(), text);
		checkBox.setEditable(editable);
	}

	public <E extends Enum<E>> CheckBoxFormElement(Set<E> key, E value, boolean editable) {
		this(new SetElementFormElementProperty(Keys.getProperty(key), value), EnumUtils.getText(value), editable);
	}

	public <E extends Enum<E>> CheckBoxFormElement(Set<E> key, E value) {
		this(key, value, Form.EDITABLE);
	}

	@Override
	public String getCaption() {
		return caption ? super.getCaption() : null;
	}

	@Override
	public boolean canBeEmpty() {
		return false;
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

	@Override
	public void setEnabled(boolean enabled) {
		checkBox.setEditable(editable && enabled);
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
		public abstract Boolean getValue(Object object);

		@Override
		public abstract void setValue(Object object, Object newValue);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static class SetElementFormElementProperty extends CheckBoxProperty {
		private final Property property;
		private final Object value;

		public SetElementFormElementProperty(Property property, Object value) {
			this.property = property;
			this.value = Objects.requireNonNull(value);
		}

		@Override
		public Boolean getValue(Object object) {
			Set set = (Set) property.getValue(object);
			return set.contains(value);
		}

		@Override
		public void setValue(Object object, Object newValue) {
			Set set = (Set) property.getValue(object);
			if (Boolean.TRUE.equals(newValue)) {
				set.add(value);
			} else {
				set.remove(value);
			}
		}

		@Override
		public Class<?> getDeclaringClass() {
			return property.getDeclaringClass();
		}

		@Override
		public String getName() {
			return property.getName();
		}

		@Override
		public String getPath() {
			return property.getPath();
		}

		@Override
		public Class<?> getGenericClass() {
			return null;
		}

		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			return property.getAnnotation(annotationClass);
		}

		@Override
		public boolean isFinal() {
			// override, otherwise all fields would be read only
			return false;
		}
	}

}

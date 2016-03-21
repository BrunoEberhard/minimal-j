package org.minimalj.frontend.form.element;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.CheckBoxFormElement.CheckBoxProperty;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.mock.Mocking;
import org.minimalj.util.resources.Resources;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class EnumSetFormElement<E extends Set<Enum<?>>> extends ObjectFormElement<E> implements Enable, Mocking {
	private final Class enumClass;
	private final Collection allowedValues;
	
	public EnumSetFormElement(PropertyInterface property, boolean editable) {
		this(property, null, editable);
	}

	public EnumSetFormElement(E key, E allowedValues) {
		this(Keys.getProperty(key), allowedValues, true);
	}
		
	public EnumSetFormElement(PropertyInterface property, E allowedValues, boolean editable) {
		super(property, editable);
		this.enumClass = GenericUtils.getGenericClass(property.getType());
		this.allowedValues = allowedValues != null ? allowedValues : EnumUtils.valueList(enumClass);
	}
	
	@Override
	public void mock() {
		E newValues = (E) new HashSet();
		for (Object object : allowedValues) {
			if (Math.random() <0.5) {
				Enum<?> value = (Enum<?>) object;
				newValues.add(value);
			}
		}
		setValue(newValues);
	}

	@Override
	protected Form<E> createForm() {
		Form<E> form = new Form<E>(true);
		for (Object object : allowedValues) {
			Enum<?> value = (Enum<?>) object;
			form.lineWithoutCaption(new CheckBoxFormElement(new EnumSetFormElementProperty(value), EnumUtils.getText((Enum) object), true));
		}
		return form;
	}

	@Override
	protected void show(E objects) {
		for (Object object : objects) {
			if (object instanceof Rendering) {
				add((Rendering) object);
			} else {
				add(object.toString());
			}
		}
	}

	@Override
	protected Action[] getActions() {
		return new Action[] { new EnumSetFormElementEditor() };
	}
	
	// EnumSetFormElementEditor is needed because the CloneHelper doesn't clone / copy Set<Enum> 
	public class EnumSetFormElementEditor extends ObjectFormElementEditor {
		public EnumSetFormElementEditor() {
			super();
		}

		@Override
		protected Object[] getNameArguments() {
			// enumClass is not yet available when this method is called
			return new Object[] { Resources.getString(GenericUtils.getGenericClass(getProperty().getType())) };
		}
		
		@Override
		public E createObject() {
			return (E) new HashSet(getValue());
		}

		@Override
		public Void save(E edited) {
			getValue().clear();
			getValue().addAll(edited);
			return null;
		}
	}
	
	private class EnumSetFormElementProperty extends CheckBoxProperty {
		private final Enum<?> value;

		public EnumSetFormElementProperty(Enum<?> value) {
			this.value = value;
		}

		@Override
		public Class<?> getDeclaringClass() {
			return enumClass;
		}

		@Override
		public String getName() {
			return value.name();
		}

		@Override
		public String getPath() {
			return value.name();
		}

		@Override
		public Class<?> getClazz() {
			return enumClass;
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

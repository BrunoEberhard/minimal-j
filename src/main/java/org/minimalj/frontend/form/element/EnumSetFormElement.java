package org.minimalj.frontend.form.element;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.CheckBoxFormElement.CheckBoxProperty;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.mock.Mocking;
import org.minimalj.util.resources.Resources;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class EnumSetFormElement<E extends Set<Enum<?>>> extends AbstractFormElement<E> implements /* Enable, */ Mocking {
	private final Class enumClass;
	private final Collection allowedValues;
	private final boolean editable;
	private final Input<E> lookup;
	
	public EnumSetFormElement(PropertyInterface property, boolean editable) {
		this(property, null, editable);
	}

	public EnumSetFormElement(E key, E allowedValues) {
		this(Keys.getProperty(key), allowedValues, true);
	}
		
	public EnumSetFormElement(PropertyInterface property, E allowedValues, boolean editable) {
		super(property);
		this.editable = editable;
		this.enumClass = property.getGenericClass();
		this.allowedValues = allowedValues != null ? allowedValues : EnumUtils.valueList(enumClass);
		this.lookup = Frontend.getInstance().createLookup(this::showForm, listener());
	}

	public void showForm() {
		new EnumSetFormElementEditor().action();
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

	public IComponent getComponent() {
		return lookup;
	};

	@Override
	public E getValue() {
		E value = lookup.getValue();
		// EnumSetFormElementEditor and the final fields expect a not null value
		// but the Lookup Input may set it to null in Remove Action
		return value != null ? value : (E) new TreeSet();
	}

	@Override
	public void setValue(E object) {
		lookup.setValue(object);
	}

	public class EnumSetFormElementEditor extends SimpleEditor<E> {
		@Override
		protected Object[] getNameArguments() {
			// enumClass is not yet available when this method is called
			return new Object[] { Resources.getString(getProperty().getGenericClass()) };
		}
		
		@Override
		public E createObject() {
			return (E) new HashSet(getValue());
		}

		@Override
		public Form<E> createForm() {
			Form<E> form = new Form<>(true);
			for (Object object : allowedValues) {
				Enum<?> value = (Enum<?>) object;
				form.lineWithoutCaption(new CheckBoxFormElement(new EnumSetFormElementProperty(value), EnumUtils.getText((Enum) object), true));
			}
			return form;
		}

		@Override
		protected E save(E object) {
			EnumSetFormElement.this.setValue(object);
			return object;
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

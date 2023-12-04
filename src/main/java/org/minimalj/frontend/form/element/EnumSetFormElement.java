package org.minimalj.frontend.form.element;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.AbstractLookupFormElement.LookupParser;
import org.minimalj.frontend.form.element.CheckBoxFormElement.CheckBoxProperty;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.Rendering;
import org.minimalj.model.properties.Property;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.StringUtils;
import org.minimalj.util.mock.Mocking;
import org.minimalj.util.resources.Resources;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class EnumSetFormElement<E extends Set<Enum<?>>> extends AbstractLookupFormElement<E> implements LookupParser, Mocking {
	private final Class enumClass;
	private final Collection allowedValues;
	
	public EnumSetFormElement(Property property, boolean editable) {
		this(property, null, editable);
	}
	
	public EnumSetFormElement(Property property, E allowedValues, boolean editable) {
		super(property, editable);
		this.enumClass = property.getGenericClass();
		this.allowedValues = allowedValues != null ? allowedValues : EnumUtils.valueList(enumClass);
	}

	public E parse(String text) {
		E value = getValue();
		value.clear();
		if (!StringUtils.isEmpty(text)) {
			String[] split = text.split(",");
			SPLITS: for (String s : split) {
				s = s.trim();
				for (Object c : allowedValues) {
					if (Rendering.render(c).equals(s)) {
						((Set) value).add(c);
						continue SPLITS;
					}
				}
				value.add(InvalidValues.createInvalidEnum(enumClass, s));
			}
		}
		return value;
	}

	public void lookup() {
		new EnumSetFormElementEditor().run();
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
			Form<E> form = new Form<>(Form.EDITABLE);
			for (Object object : allowedValues) {
				Enum<?> value = (Enum<?>) object;
				form.line(new CheckBoxFormElement(new EnumSetFormElementProperty(value), EnumUtils.getText((Enum) object), true, false));
			}
			return form; 
		}

		@Override
		protected E save(E object) {
			E values = getValue();
			values.clear();
			values.addAll(object);
			EnumSetFormElement.this.setValueInternal(values);
			return values;
		}
	}
	
	public static class EnumSetFormElementProperty extends CheckBoxProperty {
		private final Enum<?> value;

		public EnumSetFormElementProperty(Enum<?> value) {
			this.value = Objects.requireNonNull(value);
		}

		@Override
		public Class<?> getDeclaringClass() {
			return value.getClass();
		}

		@Override
		public String getName() {
			return value.name();
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

	@Override
	public void mock() {
		E values = getValue();
		values.clear();
		for (Object object : allowedValues) {
			if (Math.random() <0.5) {
				Enum<?> value = (Enum<?>) object;
				values.add(value);
			}
		}
		setValueInternal(values);
	}

}

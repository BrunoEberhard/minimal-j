package org.minimalj.frontend.form.element;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.form.Form;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.mock.Mocking;

// TODO: Typisierung bringt hier so was von nichts
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
	protected Form<E> createFormPanel() {
		Form<E> form = new Form<E>(true);
		for (Object object : allowedValues) {
			Enum<?> value = (Enum<?>) object;
			form.lineWithoutCaption(new CheckBoxFormElement(new EnumSetFieldEditorPropertyInterface(value), EnumUtils.getText((Enum) object), true));
		}
		return form;
	}

	@Override
	protected void show(E objects) {
		for (Object object : objects) {
			add(object);
		}
	}

	@Override
	protected Action[] getActions() {
		return new Action[] { new ObjectFormElementEditor() };
	}
	
	private class EnumSetFieldEditorPropertyInterface implements PropertyInterface {
		private final Enum<?> value;

		public EnumSetFieldEditorPropertyInterface(Enum<?> value) {
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
		public Type getType() {
			return null;
		}

		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			return null;
		}

		@Override
		public Object getValue(Object object) {
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

		@Override
		public boolean isFinal() {
			return false;
		}
	}

}

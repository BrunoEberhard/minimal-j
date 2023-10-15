package org.minimalj.frontend.form.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.model.CodeItem;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.properties.Property;
import org.minimalj.util.IdUtils;
import org.minimalj.util.mock.Mocking;
import org.minimalj.util.resources.Resources;

public class ComboBoxFormElement<T> extends AbstractFormElement<T> implements Enable, Mocking {

	public static final String NO_NULL_STRING = null;
	public static final String EMPTY_NULL_STRING = "";
	
	private final List<T> values;
	private final boolean canBeEmpty;
	private final Input<T> comboBox;

	public ComboBoxFormElement(T key, List<T> values) {
		this(Keys.getProperty(key), values);
	}

	public ComboBoxFormElement(T key, List<T> values, String nullText) {
		this(Keys.getProperty(key), values, nullText);
	}

	public ComboBoxFormElement(Property property, List<T> values) {
		this(property, values, EMPTY_NULL_STRING);
	}
	
	public ComboBoxFormElement(Property property, List<T> values, String nullText) {
		super(property);
		this.values = this instanceof CodeFormElement ? values : new ArrayList<>(values);
		comboBox = Frontend.getInstance().createComboBox(this.values, nullText, listener());
		canBeEmpty = nullText != null;
	}
	
	@Override
	public IComponent getComponent() {
		return comboBox;
	}

	@Override
	public void setEnabled(boolean enabled) {
		comboBox.setEditable(enabled);
	}

	@Override
	public T getValue() {
		return comboBox.getValue();
	}

	@Override
	public void setValue(T value) {
		// 'contains' uses the default equals method 
		if (value != null && !values.contains(value)) {
			// there could be a different instance with same id.
			// if yes take it.
			Object id = IdUtils.getId(value);
			value = values.stream().filter(c -> Objects.equals(id, IdUtils.getId(c))).findFirst().orElse(null);
		}
		comboBox.setValue(value);
	}
	
	@Override
	public boolean canBeEmpty() {
		return canBeEmpty;
	}

	@Override
	public void mock() {
		int index = (int)(Math.random() * values.size());
		setValue(values.get(index));
	}
	
	public static class BooleanComboBoxFormElement extends AbstractFormElement<Boolean> implements Enable, Mocking {

		private final List<CodeItem<Boolean>> values;
		private final boolean canBeEmpty;
		private final Input<CodeItem<Boolean>> comboBox;

		public BooleanComboBoxFormElement(Boolean key) {
			super(key);
			Property property = Keys.getProperty(key);
			canBeEmpty = property.getAnnotation(NotEmpty.class) == null;
			values = new ArrayList<>();
			if (canBeEmpty) {
				values.add(new CodeItem<Boolean>(null, EMPTY_NULL_STRING));
			}
			values.add(new CodeItem<>(true, Resources.getPropertyName(property, "true")));
			values.add(new CodeItem<>(false, Resources.getPropertyName(property, "false")));
			comboBox = Frontend.getInstance().createComboBox(this.values, NO_NULL_STRING, listener());
		}
		
		@Override
		public IComponent getComponent() {
			return comboBox;
		}

		@Override
		public void setEnabled(boolean enabled) {
			comboBox.setEditable(enabled);
		}

		@Override
		public Boolean getValue() {
			CodeItem<Boolean> item = comboBox.getValue();
			return item != null ? item.getKey() : null;
		}

		@Override
		public void setValue(Boolean value) {
			CodeItem<Boolean> item = values.stream().filter(c -> Objects.equals(value, c.getKey())).findFirst().orElse(null);
			comboBox.setValue(item);
		}
		
		@Override
		public boolean canBeEmpty() {
			return canBeEmpty;
		}

		@Override
		public void mock() {
			int index = (int)(Math.random() * values.size());
			comboBox.setValue(values.get(index));
		}
		
	}
}

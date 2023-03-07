package org.minimalj.frontend.form.element;

import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.model.CodeItem;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.Property;
import org.minimalj.util.mock.Mocking;

public class EnumFormElement<E extends Enum<E>> extends AbstractFormElement<E> implements Enable, Mocking {
	private final Class<E> enumClass;
	private final boolean canBeEmpty;
	
	private final Input<CodeItem<E>> comboBox;

	public EnumFormElement(Property property) {
		this(property, null, true);
	}

	public EnumFormElement(E key, List<E> canBeEmpty) {
		this(Keys.getProperty(key), canBeEmpty, true);
	}

	public EnumFormElement(E key) {
		this(key, true);
	}

	@SuppressWarnings("unchecked")
	public EnumFormElement(E key, boolean canBeEmpty) {
		this(Keys.getProperty(key), (List<E>) EnumUtils.valueList(key.getClass()), canBeEmpty);
	}
		
	@SuppressWarnings("unchecked")
	public EnumFormElement(Property property, List<E> allowedValues, boolean canBeEmpty) {
		super(property);
		this.enumClass = (Class<E>) property.getClazz();
		this.canBeEmpty = canBeEmpty;
		
		List<CodeItem<E>> itemList = allowedValues != null ? EnumUtils.itemList(allowedValues) : EnumUtils.itemList(enumClass);
		comboBox = Frontend.getInstance().createComboBox(itemList, canBeEmpty ? ComboBoxFormElement.EMPTY_NULL_STRING : ComboBoxFormElement.NO_NULL_STRING, listener());
		
		setDefault();
	}
	
	@Override
	public IComponent getComponent() {
		return comboBox;
	}

	@Override
	public void setEnabled(boolean enabled) {
		comboBox.setEditable(enabled);
	}

	private void setDefault() {
    	E defolt = EnumUtils.getDefault(enumClass);
    	if (!defolt.equals(getValue())) {
    		setValue(defolt);
    		fireChange();
    	}
	}
	
	@Override
	public E getValue() {
		if (comboBox.getValue() != null) {
			return comboBox.getValue().getKey();
		} else {
			return null;
		}
	}

	@Override
	public void setValue(E value) {
		CodeItem<E> item = null;
		if (value != null) {
			List<CodeItem<E>> itemList = EnumUtils.itemList(enumClass);
			for (CodeItem<E> i : itemList) {
				if (i.getKey().equals(value)) {
					item = i;
					break;
				}
			}
		}
		
		comboBox.setValue(item);
	}
	
	@Override
	public boolean canBeEmpty() {
		return canBeEmpty;
	}

	@Override
	public void mock() {
		if (Math.random() < 0.2) {
			setDefault();
		} else {
			List<E> valueList = EnumUtils.valueList(enumClass);
			int index = (int)(Math.random() * valueList.size());
			setValue(valueList.get(index));
		}
	}
	
}

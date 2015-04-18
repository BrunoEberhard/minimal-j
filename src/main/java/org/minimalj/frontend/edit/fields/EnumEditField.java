package org.minimalj.frontend.edit.fields;

import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.ClientToolkit.Input;
import org.minimalj.model.CodeItem;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.mock.Mocking;

// TODO: Typisierung bringt hier so was von nichts
public class EnumEditField<E extends Enum<E>> extends AbstractEditField<E> implements Enable, Mocking {
	private final Class<E> enumClass;
	
	private final Input<CodeItem<E>> comboBox;

	public EnumEditField(PropertyInterface property) {
		this(property, null);
	}

	public EnumEditField(E key, List<E> allowedValues) {
		this(Keys.getProperty(key), allowedValues);
	}
		
	@SuppressWarnings("unchecked")
	public EnumEditField(PropertyInterface property, List<E> allowedValues) {
		super(property, true);
		this.enumClass = (Class<E>) property.getClazz();
		
		List<CodeItem<E>> itemList = allowedValues != null ? EnumUtils.itemList(allowedValues) : EnumUtils.itemList(enumClass);
		comboBox = ClientToolkit.getToolkit().createComboBox(itemList, listener());
		
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
    	if (!defolt.equals(getObject())) {
    		setObject(defolt);
    		fireChange();
    	}
	}
	
	@Override
	public E getObject() {
		if (comboBox.getValue() != null) {
			return comboBox.getValue().getKey();
		} else {
			return null;
		}
	}

	@Override
	public void setObject(E value) {
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
	public void mock() {
		if (Math.random() < 0.2) {
			setDefault();
		} else {
			List<E> valueList = EnumUtils.valueList(enumClass);
			int index = (int)(Math.random() * (double)valueList.size());
			setObject(valueList.get(index));
		}
	}
	
}

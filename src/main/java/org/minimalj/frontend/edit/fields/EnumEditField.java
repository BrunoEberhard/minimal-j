package org.minimalj.frontend.edit.fields;

import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.ComboBox;
import org.minimalj.frontend.toolkit.SwitchComponent;
import org.minimalj.frontend.toolkit.TextField;
import org.minimalj.model.CodeItem;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.mock.Mocking;

// TODO: Typisierung bringt hier so was von nichts
public class EnumEditField<E extends Enum<E>> extends AbstractEditField<E> implements Enable, Mocking {
	private final Class<E> enumClass;
	
	private final SwitchComponent switchComponent;
	private final ComboBox<CodeItem<E>> comboBox;
	private final TextField textFieldDisabled;

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
		
		textFieldDisabled = ClientToolkit.getToolkit().createReadOnlyTextField();
		textFieldDisabled.setText("-");
		
		switchComponent = ClientToolkit.getToolkit().createSwitchComponent(comboBox, textFieldDisabled);
		switchComponent.show(comboBox);
		
		setDefault();
	}
	
	@Override
	public IComponent getComponent() {
		return switchComponent;
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (enabled) {
			switchComponent.show(comboBox);
			setDefault();
		} else {
			switchComponent.show(textFieldDisabled);
		}
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
		if (switchComponent.getShownComponent() == comboBox) {
			if (comboBox.getSelectedObject() != null) {
				return comboBox.getSelectedObject().getKey();
			}
		}
		return null;
	}

	@Override
	public void setObject(E value) {
		if (switchComponent.getShownComponent() == textFieldDisabled) {
			return;
		}

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
		
		comboBox.setSelectedObject(item);
		switchComponent.show(comboBox);
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

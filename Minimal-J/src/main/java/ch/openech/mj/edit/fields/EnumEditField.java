package ch.openech.mj.edit.fields;

import java.util.List;

import ch.openech.mj.autofill.DemoEnabled;
import ch.openech.mj.model.CodeItem;
import ch.openech.mj.model.Keys;
import ch.openech.mj.model.EnumUtils;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ComboBox;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.SwitchLayout;
import ch.openech.mj.toolkit.TextField;

// TODO: Typisierung bringt hier so was von nichts
public class EnumEditField<E extends Enum<E>> extends AbstractEditField<E> implements Enable, DemoEnabled {
	private final Class<E> enumClass;
	
	private final SwitchLayout switchLayout;
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
		this.enumClass = (Class<E>) property.getFieldClazz();
		
		comboBox = ClientToolkit.getToolkit().createComboBox(listener());
		comboBox.setObjects(allowedValues != null ? EnumUtils.itemList(allowedValues) : EnumUtils.itemList(enumClass));
		
		textFieldDisabled = ClientToolkit.getToolkit().createReadOnlyTextField();
		textFieldDisabled.setText("-");
		
		switchLayout = ClientToolkit.getToolkit().createSwitchLayout();
		switchLayout.show(comboBox);
		
		setDefault();
	}
	
	@Override
	public IComponent getComponent() {
		return switchLayout;
	}

	public void setEnabled(boolean enabled) {
		if (enabled) {
			switchLayout.show(comboBox);
			setDefault();
		} else {
			switchLayout.show(textFieldDisabled);
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
		if (switchLayout.getShownComponent() == comboBox) {
			if (comboBox.getSelectedObject() != null) {
				return comboBox.getSelectedObject().getKey();
			}
		}
		return null;
	}

	@Override
	public void setObject(E value) {
		if (switchLayout.getShownComponent() == textFieldDisabled) {
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
		switchLayout.show(comboBox);
	}

	@Override
	public void fillWithDemoData() {
		if (Math.random() < 0.2) {
			setDefault();
		} else {
			List<E> valueList = EnumUtils.valueList(enumClass);
			int index = (int)(Math.random() * (double)valueList.size());
			setObject(valueList.get(index));
		}
	}
	
}

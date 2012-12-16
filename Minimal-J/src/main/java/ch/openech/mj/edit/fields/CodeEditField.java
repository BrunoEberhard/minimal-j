package ch.openech.mj.edit.fields;

import java.util.List;

import ch.openech.mj.autofill.DemoEnabled;
import ch.openech.mj.db.model.CodeItem;
import ch.openech.mj.db.model.Constants;
import ch.openech.mj.db.model.EnumUtils;
import ch.openech.mj.db.model.PropertyInterface;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ComboBox;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.SwitchLayout;
import ch.openech.mj.toolkit.TextField;

// TODO: Typisierung bringt hier so was von nichts
public class CodeEditField<E extends Enum<E>> extends AbstractEditField<E> implements DemoEnabled {
	private final Class<E> enumClass;
	
	private final SwitchLayout switchLayout;
	private final ComboBox<CodeItem<E>> comboBox;
	private final TextField textFieldDisabled;

	public CodeEditField(PropertyInterface property) {
		this(property, null);
	}

	public CodeEditField(E key, List<E> allowedValues) {
		this(Constants.getProperty(key), allowedValues);
	}
		
	@SuppressWarnings("unchecked")
	public CodeEditField(PropertyInterface property, List<E> allowedValues) {
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
    	setObject(EnumUtils.getDefault(enumClass));
		fireChange();
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

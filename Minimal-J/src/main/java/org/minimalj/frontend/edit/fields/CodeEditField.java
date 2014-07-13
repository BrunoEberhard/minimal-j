package org.minimalj.frontend.edit.fields;

import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.ComboBox;
import org.minimalj.frontend.toolkit.SwitchComponent;
import org.minimalj.frontend.toolkit.TextField;
import org.minimalj.model.Code;
import org.minimalj.model.CodeItem;
import org.minimalj.model.Codes;
import org.minimalj.model.PropertyInterface;
import org.minimalj.util.DemoEnabled;

// TODO: Typisierung bringt hier so was von nichts
public class CodeEditField extends AbstractEditField<String> implements Enable, DemoEnabled {
	private final Code code;
	
	private final SwitchComponent switchComponent;
	private final ComboBox<CodeItem<String>> comboBox;
	private final TextField textFieldDisabled;

	public CodeEditField(PropertyInterface property, String prefix) {
		super(property, true);
		this.code = Codes.getCode(prefix);
		
		comboBox = ClientToolkit.getToolkit().createComboBox(listener());
		comboBox.setObjects(code.getCodeItems());
		
		textFieldDisabled = ClientToolkit.getToolkit().createReadOnlyTextField();
		textFieldDisabled.setText("-");
		
		switchComponent = ClientToolkit.getToolkit().createSwitchComponent(comboBox, textFieldDisabled);
		switchComponent.show(comboBox);
		
		// TODO wirklich immer? Und bewirkt das wirklich etwas oder wird es
		// gleich wieder überschrieben?
		setDefault();
	}
	
	@Override
	public IComponent getComponent() {
		return switchComponent;
	}

	public void setEnabled(boolean enabled) {
		if (enabled) {
			switchComponent.show(comboBox);
			setDefault();
		} else {
			switchComponent.show(textFieldDisabled);
		}
	}

	private void setDefault() {
    	setObject(code.getDefault());
		fireChange();
	}
	
	@Override
	public String getObject() {
		if (switchComponent.getShownComponent() == comboBox) {
			if (comboBox.getSelectedObject() != null) {
				return comboBox.getSelectedObject().getKey();
			}
		}
		return null;
	}

	@Override
	public void setObject(String value) {
		if (switchComponent.getShownComponent() == textFieldDisabled) {
			return;
		}

		CodeItem<String> item = null;
		if (value != null) {
			List<CodeItem<String>> itemList = code.getCodeItems();
			for (CodeItem<String> i : itemList) {
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
	public void fillWithDemoData() {
		if (Math.random() < 0.2) {
			setDefault();
		} else {
			List<CodeItem<String>> valueList = code.getCodeItems();
			int index = (int)(Math.random() * (double)valueList.size());
			setObject(valueList.get(index).getKey());
		}
	}
	
}

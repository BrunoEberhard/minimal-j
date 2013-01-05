package ch.openech.mj.edit.fields;

import java.util.List;

import ch.openech.mj.autofill.DemoEnabled;
import ch.openech.mj.db.model.Code;
import ch.openech.mj.db.model.CodeItem;
import ch.openech.mj.db.model.PropertyInterface;
import ch.openech.mj.model.Codes;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ComboBox;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.SwitchLayout;
import ch.openech.mj.toolkit.TextField;

// TODO: Typisierung bringt hier so was von nichts
public class CodeEditField extends AbstractEditField<String> implements Enable, DemoEnabled {
	private final Code code;
	
	private final SwitchLayout switchLayout;
	private final ComboBox<CodeItem<String>> comboBox;
	private final TextField textFieldDisabled;

	public CodeEditField(PropertyInterface property, String prefix) {
		super(property, true);
		this.code = Codes.getCode(prefix);
		
		comboBox = ClientToolkit.getToolkit().createComboBox(listener());
		comboBox.setObjects(code.getCodeItems());
		
		textFieldDisabled = ClientToolkit.getToolkit().createReadOnlyTextField();
		textFieldDisabled.setText("-");
		
		switchLayout = ClientToolkit.getToolkit().createSwitchLayout();
		switchLayout.show(comboBox);
		
		// TODO wirklich immer? Und bewirkt das wirklich etwas oder wird es
		// gleich wieder überschrieben?
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
    	setObject(code.getDefault());
		fireChange();
	}
	
	@Override
	public String getObject() {
		if (switchLayout.getShownComponent() == comboBox) {
			if (comboBox.getSelectedObject() != null) {
				return comboBox.getSelectedObject().getKey();
			}
		}
		return null;
	}

	@Override
	public void setObject(String value) {
		if (switchLayout.getShownComponent() == textFieldDisabled) {
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
		switchLayout.show(comboBox);
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

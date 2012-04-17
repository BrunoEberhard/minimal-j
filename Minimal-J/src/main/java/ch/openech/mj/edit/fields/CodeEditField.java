package ch.openech.mj.edit.fields;

import ch.openech.mj.autofill.DemoEnabled;
import ch.openech.mj.db.model.Code;
import ch.openech.mj.db.model.CodeItem;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ComboBox;
import ch.openech.mj.toolkit.SwitchLayout;
import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.util.StringUtils;

public class CodeEditField extends AbstractEditField<String> implements DemoEnabled {
	private final Code code;
	
	private final SwitchLayout switchLayout;
	private final ComboBox<CodeItem> comboBox;
	private final TextField textFieldDisabled;

	public CodeEditField(Object key, Code code) {
		super(key, true);
		this.code = code;

		comboBox = ClientToolkit.getToolkit().createComboBox(listener());
		comboBox.setObjects(code.getItems());
		
		textFieldDisabled = ClientToolkit.getToolkit().createReadOnlyTextField();
		textFieldDisabled.setText("-");
		
		switchLayout = ClientToolkit.getToolkit().createSwitchLayout();
		switchLayout.show(comboBox);
		
		setDefault();
	}
	
	@Override
	public Object getComponent() {
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
        if (!StringUtils.isBlank(code.getDefault())) {
        	setObject(code.getDefault());
			fireChange();
        } else {
        	setObject(null);
        }
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
		
		if (value == null) {
			comboBox.setSelectedObject(null);
		} else {
			int index = code.indexOf(value);
			if (index >= 0) {
				comboBox.setSelectedObject(code.getItem(index));
			} else {
				comboBox.setSelectedObject(new CodeItem(value, "Wert " + value));
			}
		}
		switchLayout.show(comboBox);
	}

	@Override
	public void fillWithDemoData() {
		if (Math.random() < 0.2) {
			setObject(code.getDefault());
		} else {
			int index = (int)(Math.random() * (double)code.count());
			setObject(code.getKey(index));
		}
	}
	
}

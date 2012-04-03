package ch.openech.mj.edit.fields;

import ch.openech.mj.autofill.DemoEnabled;
import ch.openech.mj.db.model.Code;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ComboBox;
import ch.openech.mj.toolkit.SwitchLayout;
import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.util.StringUtils;

public class CodeEditField extends AbstractEditField<String> implements DemoEnabled, Indicator {
	private final Code code;
	
	private final SwitchLayout switchLayout;
	private final ComboBox comboBox;
	private final TextField textFieldDisabled;

	public CodeEditField(Object key, Code code) {
		super(key, true);
		this.code = code;

		comboBox = ClientToolkit.getToolkit().createComboBox(listener());
		comboBox.setObjects(code.getTexts());
		
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
			String text = (String) comboBox.getSelectedObject();
			return code.getKey(text);
		} else {
			return null;
		}
	}

	@Override
	public void setObject(String value) {
		if (switchLayout.getShownComponent() == textFieldDisabled) {
			return;
		}
		
		int index = code.indexOf(value);
		if (index >= 0) {
			comboBox.setSelectedObject(code.getText(index));
			switchLayout.show(comboBox);
		} else {
			
		}
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

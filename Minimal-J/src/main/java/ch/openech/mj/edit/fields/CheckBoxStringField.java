package ch.openech.mj.edit.fields;

import java.util.List;

import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.toolkit.CheckBox;
import ch.openech.mj.toolkit.ClientToolkit;


public class CheckBoxStringField extends AbstractEditField<String> {
	
	private final CheckBox checkBox;
	
	public CheckBoxStringField(Object key, String text, boolean editable) {
		super(key);
		checkBox = ClientToolkit.getToolkit().createCheckBox(listener(), text);
		checkBox.setEnabled(editable);
	}
	
	@Override
	public Object getComponent() {
		return checkBox;
	}
	
	@Override
	public String getObject() {
		return checkBox.isSelected() ? "1" : "0";
	}		
	
	@Override
	public void setObject(String value) {
		checkBox.setSelected("1".equals(value));
	}

	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		// TODO Validierung für CheckBoxFelder
	}

}

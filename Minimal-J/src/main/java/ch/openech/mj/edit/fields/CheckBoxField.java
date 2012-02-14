package ch.openech.mj.edit.fields;

import java.util.List;

import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.toolkit.CheckBox;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponentDelegate;

public class CheckBoxField extends AbstractEditField<Boolean> {
	
	private final CheckBox checkBox;
	
	public CheckBoxField(Object key, String text) {
		super(key);
		checkBox = ClientToolkit.getToolkit().createCheckBox(text);
		listenTo(checkBox);
	}
	
	@Override
	public Object getComponent() {
		return checkBox;
	}
	
	@Override
	public Boolean getObject() {
		return checkBox.isSelected();
	}		
	
	@Override
	public void setObject(Boolean value) {
		checkBox.setSelected(Boolean.TRUE.equals(value));
	}

	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		// TODO Validierung für CheckBoxFelder
	}
}

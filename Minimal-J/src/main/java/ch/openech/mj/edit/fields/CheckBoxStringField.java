package ch.openech.mj.edit.fields;

import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.toolkit.CheckBox;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;

public class CheckBoxStringField extends AbstractEditField<Boolean> {
	private final CheckBox checkBox;
	
	public CheckBoxStringField(PropertyInterface property, String text, boolean editable) {
		super(property, editable);
		checkBox = ClientToolkit.getToolkit().createCheckBox(listener(), text);
		checkBox.setEnabled(editable);
	}
	
	@Override
	public IComponent getComponent() {
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

}

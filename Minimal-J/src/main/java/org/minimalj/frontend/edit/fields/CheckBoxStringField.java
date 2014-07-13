package org.minimalj.frontend.edit.fields;

import org.minimalj.frontend.toolkit.CheckBox;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.model.PropertyInterface;

public class CheckBoxStringField extends AbstractEditField<Boolean> {
	private final CheckBox checkBox;
	
	public CheckBoxStringField(PropertyInterface property, String text, boolean editable) {
		super(property, editable);
		checkBox = ClientToolkit.getToolkit().createCheckBox(listener(), text);
		checkBox.setEditable(editable);
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

package org.minimalj.frontend.edit.fields;

import org.minimalj.frontend.toolkit.CheckBox;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.model.properties.PropertyInterface;

public class CheckBoxField extends AbstractEditField<Boolean> {
	
	private final CheckBox checkBox;
	
	public CheckBoxField(PropertyInterface property, String text) {
		super(property, true);
		checkBox = ClientToolkit.getToolkit().createCheckBox(listener(), text);
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

package org.minimalj.frontend.edit.fields;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.ClientToolkit.Input;
import org.minimalj.model.properties.PropertyInterface;

public class CheckBoxField extends AbstractEditField<Boolean> {
	private final Input<Boolean> checkBox;
	
	public CheckBoxField(PropertyInterface property, String text, boolean editable) {
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
		return checkBox.getValue();
	}		
	
	@Override
	public void setObject(Boolean value) {
		checkBox.setValue(Boolean.TRUE.equals(value));
	}

}

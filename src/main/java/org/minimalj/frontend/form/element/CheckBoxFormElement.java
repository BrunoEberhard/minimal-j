package org.minimalj.frontend.form.element;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.ClientToolkit.Input;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.resources.Resources;

public class CheckBoxFormElement extends AbstractFormElement<Boolean> {
	private final Input<Boolean> checkBox;
	
	public CheckBoxFormElement(PropertyInterface property, boolean editable) {
		 this(property, Resources.getObjectFieldName(Resources.getResourceBundle(), property, ".checkBoxText"), editable);
	}
	 
	public CheckBoxFormElement(PropertyInterface property, String text, boolean editable) {
		super(property);
		checkBox = ClientToolkit.getToolkit().createCheckBox(listener(), text);
		checkBox.setEditable(editable);
	}
	
	@Override
	public IComponent getComponent() {
		return checkBox;
	}
	
	@Override
	public Boolean getValue() {
		return checkBox.getValue();
	}		
	
	@Override
	public void setValue(Boolean value) {
		checkBox.setValue(Boolean.TRUE.equals(value));
	}

}

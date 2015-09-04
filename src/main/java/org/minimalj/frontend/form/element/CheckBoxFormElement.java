package org.minimalj.frontend.form.element;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.resources.Resources;

public class CheckBoxFormElement extends AbstractFormElement<Boolean> {
	private final Input<Boolean> checkBox;
	
	public CheckBoxFormElement(PropertyInterface property, boolean editable) {
		 this(property, Resources.getObjectFieldName(property, ".checkBoxText"), editable);
	}
	 
	public CheckBoxFormElement(PropertyInterface property, String text, boolean editable) {
		super(property);
		checkBox = Frontend.getInstance().createCheckBox(listener(), text);
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

package ch.openech.mj.edit.fields;

import ch.openech.mj.toolkit.CheckBox;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;

// TODO Validierung für CheckBoxFelder
public class CheckBoxField extends AbstractEditField<Boolean> {
	
	private final CheckBox checkBox;
	
	public CheckBoxField(Object key, String text) {
		super(key);
		checkBox = ClientToolkit.getToolkit().createCheckBox(listener(), text);
	}
	
	@Override
	public IComponent getComponent0() {
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

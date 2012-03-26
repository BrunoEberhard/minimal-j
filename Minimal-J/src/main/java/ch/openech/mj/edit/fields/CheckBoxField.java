package ch.openech.mj.edit.fields;

import ch.openech.mj.toolkit.CheckBox;
import ch.openech.mj.toolkit.ClientToolkit;

// TODO Validierung für CheckBoxFelder
public class CheckBoxField extends AbstractEditField<Boolean> {
	
	private final CheckBox checkBox;
	
	public CheckBoxField(Object key, String text) {
		super(key, true);
		checkBox = ClientToolkit.getToolkit().createCheckBox(listener(), text);
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

}

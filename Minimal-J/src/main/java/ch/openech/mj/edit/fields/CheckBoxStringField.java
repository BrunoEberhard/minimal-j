package ch.openech.mj.edit.fields;

import ch.openech.mj.toolkit.CheckBox;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;

// TODO Validierung für CheckBoxFelder
public class CheckBoxStringField extends AbstractEditField<String> {
	
	private final CheckBox checkBox;
	
	public CheckBoxStringField(Object key, String text, boolean editable) {
		super(key);
		checkBox = ClientToolkit.getToolkit().createCheckBox(listener(), text);
		checkBox.setEnabled(editable);
	}
	
	@Override
	public IComponent getComponent0() {
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

}

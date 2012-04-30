package ch.openech.mj.edit.fields;

import ch.openech.mj.toolkit.CheckBox;
import ch.openech.mj.toolkit.ClientToolkit;

public class CheckBoxStringField extends AbstractEditField<String> {
	private final CheckBox checkBox;
	
	public CheckBoxStringField(Object key, String text, boolean editable) {
		super(key, editable);
		checkBox = ClientToolkit.getToolkit().createCheckBox(listener(), text);
		checkBox.setEnabled(editable);
	}
	
	@Override
	public Object getComponent() {
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

	public static boolean isTrue(Object value) {
		return "1".equals(value);
	}
}

package ch.openech.mj.edit.fields;

import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ClientToolkit.InputComponentListener;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.TextField;


public class TextEditField implements EditField<String>, Enable {

	private final PropertyInterface property;
	private final int maxLength;
	private final TextField textField;
	private EditFieldListener changeListener;

	public TextEditField(PropertyInterface property, int maxLength) {
		this.property =  property;
		this.maxLength = maxLength;
		this.textField = ClientToolkit.getToolkit().createTextField(new ForwardingChangeListener(), maxLength);
	}

	@Override
	public PropertyInterface getProperty() {
		return property;
	}

	@Override
	public IComponent getComponent() {
		return textField;
	}

	@Override
	public void setObject(String string) {
		if (string != null) {
			if (maxLength > 0 && string.length() > maxLength) {
				string = string.substring(0, maxLength);
			}
		}
		textField.setText(string);
	}

	@Override
	public String getObject() {
		return textField.getText();
	}

	@Override
	public void setChangeListener(EditFieldListener changeListener) {
		if (changeListener == null) {
			throw new IllegalArgumentException("ChangeListener must not be null");
		}
		if (this.changeListener != null) {
			throw new IllegalStateException("ChangeListener can only be set once");
		}
		this.changeListener = changeListener;
	}
	
	private class ForwardingChangeListener implements InputComponentListener {
		@Override
		public void changed(IComponent source) {
			if (changeListener != null) {
				changeListener.changed(TextEditField.this);
			}
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		textField.setEditable(enabled);
	}
	
}

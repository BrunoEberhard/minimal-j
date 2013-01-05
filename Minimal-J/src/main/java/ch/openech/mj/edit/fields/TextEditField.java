package ch.openech.mj.edit.fields;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.autofill.DemoEnabled;
import ch.openech.mj.autofill.FirstNameGenerator;
import ch.openech.mj.autofill.NameGenerator;
import ch.openech.mj.db.model.PropertyInterface;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.TextField;


public class TextEditField implements EditField<String>, Enable, DemoEnabled {

	private final PropertyInterface property;
	private final int maxLength;
	private final TextField textField;
	private ChangeListener changeListener;

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
	public void setChangeListener(ChangeListener changeListener) {
		if (changeListener == null) {
			throw new IllegalArgumentException("ChangeListener must not be null");
		}
		if (this.changeListener != null) {
			throw new IllegalStateException("ChangeListener can only be set once");
		}
		this.changeListener = changeListener;
	}

	@Override
	public void fillWithDemoData() {
		String name = property.getFieldName();
		
		// if (numeric) setObject(NumberGenerator.generate(minLength, maxLength));
		/* else */ if (name.startsWith("street")) setObject(NameGenerator.street());
		else if (name.endsWith("fatherFirstName")) setObject(FirstNameGenerator.getFirstName(true));
		else if (name.endsWith("motherFirstName")) setObject(FirstNameGenerator.getFirstName(false));
		else if (name.equals("callName")) setObject("Lorem Ipsum");
		else if (name.endsWith("Name")) setObject(NameGenerator.officialName());
	}
	
	private class ForwardingChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			if (changeListener != null) {
				changeListener.stateChanged(new ChangeEvent(TextEditField.this));
			}
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		textField.setEnabled(enabled);
	}
	
}

package org.minimalj.frontend.edit.fields;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.IComponent;
import org.minimalj.frontend.toolkit.TextField;
import org.minimalj.model.PropertyInterface;
import org.minimalj.model.annotation.StringLimitation;
import org.minimalj.util.DemoEnabled;
import org.minimalj.util.FieldUtils;

public class TextFormatField extends AbstractEditField<StringLimitation> implements DemoEnabled {
	private final TextField textField;
	private final StringLimitation textFormat;
	private StringLimitation value;

	public TextFormatField(PropertyInterface property, StringLimitation textFormat, boolean editable) {
		super(property, editable);
		this.textFormat = textFormat;
		if (editable) {
			textField = ClientToolkit.getToolkit().createTextField(listener(), textFormat.getMaxLength(), textFormat.getAllowedCharacters());
		} else {
			textField = ClientToolkit.getToolkit().createReadOnlyTextField();
		}
	}
	
	@Override
	public IComponent getComponent() {
		return textField;
	}

	@Override
	public StringLimitation getObject() {
		FieldUtils.setValue(value, textField.getText());
		return value;
	}		
	
	@Override
	public void setObject(StringLimitation value) {
		this.value = value;
		textField.setText((String) FieldUtils.getValue(value));
	}

	@Override
	public void fillWithDemoData() {
		if (value == null) {
			try {
				value = textFormat.getClass().newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		if (value instanceof DemoEnabled) {
			((DemoEnabled) value).fillWithDemoData();
		}
		setObject(value);
	}

}

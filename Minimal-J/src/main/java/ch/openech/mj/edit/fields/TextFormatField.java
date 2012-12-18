package ch.openech.mj.edit.fields;

import ch.openech.mj.autofill.DemoEnabled;
import ch.openech.mj.db.model.PropertyInterface;
import ch.openech.mj.model.annotation.LimitedString;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.util.FieldUtils;

public class TextFormatField extends AbstractEditField<LimitedString> implements DemoEnabled {
	private final TextField textField;
	private final LimitedString textFormat;
	private LimitedString value;

	public TextFormatField(PropertyInterface property, LimitedString textFormat, boolean editable) {
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
	public LimitedString getObject() {
		FieldUtils.setValue(value, textField.getText());
		return value;
	}		
	
	@Override
	public void setObject(LimitedString value) {
		this.value = value;
		textField.setText((String) FieldUtils.getValue(value));
	}

	@Override
	public void fillWithDemoData() {
		if (textFormat instanceof DemoEnabled) {
			((DemoEnabled) textFormat).fillWithDemoData();
		}
	}

}

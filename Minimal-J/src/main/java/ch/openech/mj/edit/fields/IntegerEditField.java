package ch.openech.mj.edit.fields;

import java.util.Random;

import ch.openech.mj.autofill.DemoEnabled;
import ch.openech.mj.db.model.InvalidValues;
import ch.openech.mj.db.model.PropertyInterface;


public class IntegerEditField extends NumberEditField<Integer> implements DemoEnabled {

	public IntegerEditField(PropertyInterface property, int size, boolean negative) {
		super(property, size, 0, negative);
	}

	@Override
	public void setObject(Integer number) {
		String text = null;
		if (number != null) {
			if (InvalidValues.isInvalid(number)) {
				text = InvalidValues.getInvalidValue(number);
			} else {
				text = number.toString();
			}
		}
		textField.setText(text);
	}

	@Override
	public Integer getObject() {
		String text = textField.getText();
		if (text != null) {
			try {
				return Integer.parseInt(text);
			} catch (NumberFormatException nfe) {
				return InvalidValues.createInvalidInteger(text);
			}
		} else {
			return null;
		}
	}

	@Override
	public void fillWithDemoData() {
		Random random = new Random();
		int value = random.nextInt(10 ^ size);
		if (!negative || random.nextBoolean()) {
			setObject(value);
		} else {
			setObject(-value);
		}
	}

}

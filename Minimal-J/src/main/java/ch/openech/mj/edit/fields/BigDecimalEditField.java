package ch.openech.mj.edit.fields;

import java.math.BigDecimal;

import ch.openech.mj.db.model.InvalidValues;
import ch.openech.mj.db.model.PropertyInterface;


public class BigDecimalEditField extends NumberEditField<BigDecimal> /* implements DemoEnabled */ {

	public BigDecimalEditField(PropertyInterface property, int size, boolean negative) {
		super(property, size, 0, negative);
	}

	@Override
	public void setObject(BigDecimal number) {
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
	public BigDecimal getObject() {
		String text = textField.getText();
		if (text != null) {
			try {
				return new BigDecimal(text);
			} catch (NumberFormatException nfe) {
				return new BigDecimal(0);
			}
		} else {
			return null;
		}
	}

//	@Override
//	public void fillWithDemoData() {
//		Random random = new Random();
//		int value = random.nextInt(10 ^ size);
//		if (!negative || random.nextBoolean()) {
//			setObject(value);
//		} else {
//			setObject(-value);
//		}
//	}

}

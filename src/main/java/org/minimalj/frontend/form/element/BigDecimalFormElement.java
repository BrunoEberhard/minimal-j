package org.minimalj.frontend.form.element;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;

import org.minimalj.model.Keys;
import org.minimalj.model.properties.Property;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.StringUtils;
import org.minimalj.util.mock.Mocking;


public class BigDecimalFormElement extends NumberFormElement<BigDecimal> implements Mocking {

	private final NumberFormat format;
	
	public BigDecimalFormElement(BigDecimal key, boolean editable) {
		this(Keys.getProperty(key), editable);
	}
	
	public BigDecimalFormElement(Property property, boolean editable) {
		super(property, editable);
		format = createFormat(property);
	}

	protected NumberFormat createFormat(Property property) {
		NumberFormat format = new DecimalFormat();
		if (decimalPlaces > 0) {
			format.setMaximumFractionDigits(decimalPlaces);
			format.setMinimumFractionDigits(minDecimalPlaces);
		} else {
			format.setMaximumFractionDigits(0);
			format.setMinimumFractionDigits(0);
		}
		format.setGroupingUsed(false);
		return format;
	}
	
	@Override
	public BigDecimal parse(String text) {
		if (!StringUtils.isEmpty(text)) {
			try {
				BigDecimal value = new BigDecimal(text);
				if (value.signum() < 0 && !this.signed) {
					return InvalidValues.createInvalidBigDecimal(text);
				}
				value = value.stripTrailingZeros();
				if (value.precision() > this.size) {
					return InvalidValues.createInvalidBigDecimal(text);
				}
				if (value.scale() > this.decimalPlaces) {
					return InvalidValues.createInvalidBigDecimal(text);
				}
				return value;
			} catch (NumberFormatException nfe) {
				return InvalidValues.createInvalidBigDecimal(text);
			}
		} else {
			return null;
		}
	}
	
	@Override
	public String render(BigDecimal number) {
		if (number != null) {
			BigDecimal correctScale = number.setScale(format.getMaximumFractionDigits(), RoundingMode.DOWN);
			return format.format(correctScale);
		} else {
			return null;
		}
	}

	
	@Override
	public void mock() {
		Random random = new Random();
		StringBuilder s = new StringBuilder(size);
		for (int i = 0; i<size; i++) {
			s.append((char)('0' + random.nextInt(10)));
		}
		BigDecimal value = new BigDecimal(s.toString());
		value = value.movePointLeft(decimalPlaces);
		if (signed && random.nextBoolean()) {
			value = value.negate();
		}
		setValue(value);
	}

}
package ch.openech.mj.edit.fields;

import ch.openech.mj.model.InvalidValues;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ClientToolkit.InputComponentListener;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.TextField;


public abstract class NumberEditField<T> implements EditField<T> {

	private final PropertyInterface property;
	protected final boolean negative;
	protected final int size, decimalPlaces;
	
	protected final TextField textField;
	private EditFieldListener changeListener;
	
	protected NumberEditField(PropertyInterface property, int size, int decimalPlaces, boolean negative) {
		this.property = property;
		this.size = size;
		this.decimalPlaces = decimalPlaces;
		this.negative = negative;
		this.textField = ClientToolkit.getToolkit().createTextField(new ForwardingChangeListener(), getMaxLength(), getAllowedCharacters());
	}

	@Override
	public void setObject(T number) {
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
	public PropertyInterface getProperty() {
		return property;
	}

	@Override
	public IComponent getComponent() {
		return textField;
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
				changeListener.changed(NumberEditField.this);
			}
		}
	}

//	@Override
//	public void validate(List<ValidationMessage> list) {
//		if (clazz == BigDecimal.class) {
//			String text = textField.getText();
//			if (!StringUtils.isEmpty(text)) {
//				try {
//					new BigDecimal(text);
//				} catch (NumberFormatException x) {
//					list.add(new ValidationMessage(getName(), "Ungültig"));
//				}
//
//				int index = text.indexOf('.');
//				if (index >= 0) {
//					if (index > size - decimalPlaces) {
//						list.add(new ValidationMessage(getName(), "Zu gross"));
//					}
//					if (text.length() - index - 1 > decimalPlaces) {
//						list.add(new ValidationMessage(getName(), "Zu präzis"));
//					}
//				} else {
//					if (text.length() > size - decimalPlaces) {
//						list.add(new ValidationMessage(getName(), "Zu gross"));
//					}
//				}
//			}
//		}
//	}
	
	private String getAllowedCharacters() {
		if (decimalPlaces > 0) {
			return negative ? "-0123456789." : "0123456789.";
		} else {
			return negative ? "-0123456789" : "0123456789";
		}
	}
	
	public int getMaxLength() {
		return size + (negative ? 1 : 0) + (decimalPlaces > 0 ? 1 : 0);
	}

}

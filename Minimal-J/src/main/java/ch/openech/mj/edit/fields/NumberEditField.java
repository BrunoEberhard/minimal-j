package ch.openech.mj.edit.fields;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.db.model.PropertyInterface;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.toolkit.TextField.TextFieldFilter;


public abstract class NumberEditField<T> implements EditField<T> {

	private final PropertyInterface property;
	protected final boolean negative;
	protected final int size, decimalPlaces;
	
	protected final TextField textField;
	private ChangeListener changeListener;
	
	protected NumberEditField(PropertyInterface property, int size, int decimalPlaces, boolean negative) {
		this.property = property;
		this.size = size;
		this.decimalPlaces = decimalPlaces;
		this.negative = negative;
		this.textField = ClientToolkit.getToolkit().createTextField(new ForwardingChangeListener(), new NumberTextFieldFilter(size, decimalPlaces, negative));
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
	public void setChangeListener(ChangeListener changeListener) {
		if (changeListener == null) {
			throw new IllegalArgumentException("ChangeListener must not be null");
		}
		if (this.changeListener != null) {
			throw new IllegalStateException("ChangeListener can only be set once");
		}
		this.changeListener = changeListener;
	}
	
	private class ForwardingChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			if (changeListener != null) {
				changeListener.stateChanged(new ChangeEvent(NumberEditField.this));
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
	
	private static class NumberTextFieldFilter implements TextFieldFilter {
		private final int size, decimalPlaces;
		private final String allowedCharacters;
		private final boolean negative;
		
		public NumberTextFieldFilter(int size, int decimalPlaces, boolean negative) {
			this.size = size;
			this.decimalPlaces = decimalPlaces;
			this.negative = negative;
			if (decimalPlaces > 0) {
				allowedCharacters = negative ? "-0123456789." : "0123456789.";
			} else {
				allowedCharacters = negative ? "-0123456789" : "0123456789";
			}
		}

		@Override
		public int getLimit() {
			return size + (negative ? 1 : 0) + (decimalPlaces > 0 ? 1 : 0);
		}

		@Override
		public String getAllowedCharacters() {
			return allowedCharacters;
		}

	}
}

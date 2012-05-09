package ch.openech.mj.edit.fields;

import java.math.BigDecimal;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.autofill.DemoEnabled;
import ch.openech.mj.edit.validation.Validatable;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.toolkit.TextField.TextFieldFilter;
import ch.openech.mj.util.StringUtils;


public class NumberEditField implements EditField<Object>, Validatable, DemoEnabled {

	private final String name;
	private final int size, decimalPlaces;
	private final Class<?> clazz;
	
	private final TextField textField;
	private ChangeListener changeListener;
	
	public NumberEditField(String name, Class<?> clazz, int size, int decimalPlaces, boolean negative) {
		this.name = name;
		this.clazz = clazz;
		this.size = size;
		this.decimalPlaces = decimalPlaces;
		this.textField = ClientToolkit.getToolkit().createTextField(new ForwardingChangeListener(), new NumberTextFieldFilter(size, decimalPlaces, negative));
	}


	@Override
	public String getName() {
		return name;
	}

	@Override
	public IComponent getComponent() {
		return textField;
	}

	@Override
	public void setObject(Object number) {
		textField.setText(number != null ? number.toString() : null);
	}

	@Override
	public Object getObject() {
		return textField.getText();
	}

	@Override
	public boolean isEmpty() {
		return StringUtils.isEmpty(textField.getText());
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

	@Override
	public void fillWithDemoData() {
		if (clazz == Integer.class) {
			// TODO
		}
	}

	@Override
	public void validate(List<ValidationMessage> list) {
		if (clazz == BigDecimal.class) {
			String text = textField.getText();
			if (!StringUtils.isEmpty(text)) {
				try {
					new BigDecimal(text);
				} catch (NumberFormatException x) {
					list.add(new ValidationMessage(getName(), "Ungültig"));
				}

				int index = text.indexOf('.');
				if (index >= 0) {
					if (index > size - decimalPlaces) {
						list.add(new ValidationMessage(getName(), "Zu gross"));
					}
					if (text.length() - index - 1 > decimalPlaces) {
						list.add(new ValidationMessage(getName(), "Zu präzis"));
					}
				} else {
					if (text.length() > size - decimalPlaces) {
						list.add(new ValidationMessage(getName(), "Zu gross"));
					}
				}
			}
		}
	}
	
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

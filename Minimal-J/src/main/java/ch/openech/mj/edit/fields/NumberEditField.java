package ch.openech.mj.edit.fields;

import java.math.BigDecimal;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.autofill.DemoEnabled;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponentDelegate;
import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.toolkit.TextField.TextFieldFilter;
import ch.openech.mj.util.StringUtils;


public class NumberEditField implements IComponentDelegate, EditField<Object>, DemoEnabled {

	private final String name;
	private final Class<?> clazz;
	
	private final TextField textField;
	private ChangeListener changeListener;
	
	public NumberEditField(String name, Class<?> clazz, int size, boolean nonNegative) {
		this.name = name;
		this.clazz = clazz;
		this.textField = ClientToolkit.getToolkit().createTextField(new ForwardingChangeListener(), new LimitTextFieldFilter(size, nonNegative));
	}


	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getComponent() {
		return textField;
	}

	@Override
	public void setObject(Object number) {
		textField.setText(number != null ? number.toString() : null);
	}

	@Override
	public Object getObject() {
		String text = textField.getText();
		if (clazz == String.class) {
			return text;
		} else if (clazz == Integer.class) {
			return StringUtils.isEmpty(text) ? Integer.valueOf(0) : Integer.parseInt(text);
		} else if (clazz == BigDecimal.class) {
			return StringUtils.isEmpty(text) ? BigDecimal.ZERO : new BigDecimal(text);
		} else {
			throw new IllegalStateException("Clazz of NumberEditField unknown");
		}
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

	private static class LimitTextFieldFilter implements TextFieldFilter {
		private final int limit;
		private final boolean nonNegative;

		public LimitTextFieldFilter(int limit, boolean nonNegative) {
			this.limit = limit;
			this.nonNegative = nonNegative;
		}

		@Override
		public int getLimit() {
			return limit;
		}

		@Override
		public String getAllowedCharacters() {
			return nonNegative ? "0123456789" : "-0123456789";
		}

	}
}

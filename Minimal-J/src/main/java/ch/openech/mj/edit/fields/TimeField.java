package ch.openech.mj.edit.fields;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;

import ch.openech.mj.autofill.DemoEnabled;
import ch.openech.mj.model.InvalidValues;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.util.DateUtils;
import ch.openech.mj.util.StringUtils;

public class TimeField extends AbstractEditField<LocalTime> implements Enable, DemoEnabled {
	private final TextField textField;
	private final DateTimeFormatter formatter;
	
	public TimeField(PropertyInterface property, boolean editable) {
		super(property, editable);
		formatter = DateUtils.getTimeFormatter(property);
		if (editable) {
			textField = ClientToolkit.getToolkit().createTextField(listener(), 10, "01234567890:");
			installFocusLostListener();
		} else {
			textField = ClientToolkit.getToolkit().createReadOnlyTextField();
		}
	}
	
	@Override
	public IComponent getComponent() {
		return textField;
	}

	private void installFocusLostListener() {
        textField.setFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				// Formattierung auslösen
				LocalTime value = getObject();
				if (value != null && !InvalidValues.isInvalid(value)) {
					setObject(value);
				}
			}
		});
	}
	
	@Override
	public LocalTime getObject() {
		String text = textField.getText();
		if (text != null) {
			try {
				return formatter.parseLocalTime(text);
			} catch (IllegalArgumentException iae) {
				return InvalidValues.createInvalidLocalTime(text);
			}
		} else {
			return null;
		}
	}
	
	@Override
	public void setObject(LocalTime value) {
		if (InvalidValues.isInvalid(value)) {
			String text = InvalidValues.getInvalidValue(value);
			textField.setText(text);
		} else if (value != null) {
			String text = formatter.print(value);
			if (!StringUtils.equals(textField.getText(), text)) {
				textField.setText(text);
			}
		} else {
			textField.setText(null);
		}
	}

	public void setEnabled(boolean enabled) {
		textField.setEnabled(enabled);
		if (!enabled) {
			setObject(null);
		}
	}
	
	@Override
	public void fillWithDemoData() {
		setObject(generateRandom());
	}

	public LocalTime generateRandom() {
		int hour =(int)(Math.random() * 24);
		int minute =(int)(Math.random() * 60);
		if (formatter == DateUtils.TIME_FORMAT) {
			return new LocalTime(hour, minute);
		} else {
			int second =(int)(Math.random() * 60);
			if (formatter == DateUtils.TIME_FORMAT_WITH_SECONDS) {
				return new LocalTime(hour, minute, second);
			} else {
				int milis =(int)(Math.random() * 1000);
				return new LocalTime(hour, minute, second, milis);
			}
		}
	}
	
}

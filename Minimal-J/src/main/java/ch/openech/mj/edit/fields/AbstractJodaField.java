package ch.openech.mj.edit.fields;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import ch.openech.mj.autofill.DateGenerator;
import ch.openech.mj.autofill.DemoEnabled;
import ch.openech.mj.model.InvalidValues;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.model.annotation.Size;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.util.DateUtils;
import ch.openech.mj.util.StringUtils;


public abstract class AbstractJodaField<T> extends AbstractEditField<T> implements Enable, DemoEnabled {
	
	protected final TextField textField;
	
	public AbstractJodaField(PropertyInterface property) {
		this(property, false);
	}
	
	public AbstractJodaField(PropertyInterface property, boolean editable) {
		super(property, editable);
		if (editable) {
			textField = ClientToolkit.getToolkit().createTextField(listener(), getAllowedSize(property), getAllowedCharacters(property));
			installFocusLostListener();
		} else {
			textField = ClientToolkit.getToolkit().createReadOnlyTextField();
		}
	}

	protected abstract String getAllowedCharacters(PropertyInterface property);

	protected abstract int getAllowedSize(PropertyInterface property);

	@Override
	public IComponent getComponent() {
		return textField;
	}

	public abstract T getObject();
	
	public abstract void setObject(T value);
		
	private void installFocusLostListener() {
        textField.setFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				// Formattierung auslösen
				T value = getObject();
				if (value != null && !InvalidValues.isInvalid(value)) {
					setObject(value);
				}
			}
		});
	}
	

	public void setEnabled(boolean enabled) {
		textField.setEditable(enabled);
//		if (!enabled) {
//			setObject(null);
//		}
	}
	
	
	public static class JodaPartialField extends AbstractJodaField<ReadablePartial> {

		public JodaPartialField(PropertyInterface property, boolean editable) {
			super(property, editable);
		}
		
		@Override
		protected String getAllowedCharacters(PropertyInterface property) {
			return "01234567890.";
		} 
		
		@Override
		protected int getAllowedSize(PropertyInterface property) {
			return 10;
		}

		@Override
		public ReadablePartial getObject() {
			String text = textField.getText();
			text = DateUtils.parseCH(text, true);
			boolean fieldTextWasEmpty = text == null;
			if (fieldTextWasEmpty) return null;
			boolean fieldTextWasUnparsable = text.length() == 0;
			if (fieldTextWasUnparsable) return InvalidValues.createInvalidPartial(text);

			return DateUtils.parsePartial(text);
		}
		
		@Override
		public void setObject(ReadablePartial value) {
			if (InvalidValues.isInvalid(value)) {
				String text = InvalidValues.getInvalidValue(value);
				textField.setText(text);
			} else if (value != null) {
				String text = DateUtils.formatPartialCH(value);
				if (!StringUtils.equals(textField.getText(), text)) {
					textField.setText(text);
				}
			} else {
				textField.setText(null);
			}
		}

		@Override
		public void fillWithDemoData() {
			setObject(DateGenerator.generateRandomDate());
		}

	}
	
	public static class JodaDateField extends AbstractJodaField<LocalDate> {
		private static final DateTimeFormatter DATE_FORMATTER = ISODateTimeFormat.date();

		public JodaDateField(PropertyInterface property, boolean editable) {
			super(property, editable);
		}
		
		@Override
		protected String getAllowedCharacters(PropertyInterface property) {
			return "01234567890.";
		} 

		@Override
		protected int getAllowedSize(PropertyInterface property) {
			return 10;
		}

		@Override
		public LocalDate getObject() {
			String fieldText = textField.getText();
			// TODO DateField doesn't handle date locals other than CH/DE
			String text = DateUtils.parseCH(fieldText, false);
			boolean fieldTextWasEmpty = text == null;
			if (fieldTextWasEmpty) return null;
			boolean fieldTextWasUnparsable = text.length() == 0;
			if (fieldTextWasUnparsable) return InvalidValues.createInvalidLocalDate(fieldText);
			try {
				return DATE_FORMATTER.parseLocalDate(text);
			} catch (IllegalArgumentException iae) {
				// Should not really happen because parseCH should already handle this cases
				return InvalidValues.createInvalidLocalDate(fieldText);
			}
		}
		
		@Override
		public void setObject(LocalDate value) {
			if (InvalidValues.isInvalid(value)) {
				String text = InvalidValues.getInvalidValue(value);
				textField.setText(text);
			} else if (value != null) {
				String text = DateTimeFormat.mediumDate().print(value);
				if (!StringUtils.equals(textField.getText(), text)) {
					textField.setText(text);
				}
			} else {
				textField.setText(null);
			}
		}
		
		@Override
		public void fillWithDemoData() {
			if ("dateOfDeath".equals(getProperty().getFieldName())) {
				if (Math.random() < 0.9) {
					setObject(null);
					return;
				}
			}
			setObject(DateGenerator.generateRandomDate());
		}

	}

	public static class JodaTimeField extends AbstractJodaField<LocalTime> {
		private final DateTimeFormatter formatter;
		private final int size;
		
		public JodaTimeField(PropertyInterface property, boolean editable) {
			super(property, editable);
			formatter = DateUtils.getTimeFormatter(property);
			size = property.getAnnotation(Size.class).value();
		}
		
		@Override
		protected String getAllowedCharacters(PropertyInterface property) {
			int size = property.getAnnotation(Size.class).value();
			if (size > Size.TIME_WITH_SECONDS) {
				return "01234567890:.";
			} else {
				return "01234567890:";
			}
		} 

		@Override
		protected int getAllowedSize(PropertyInterface property) {
			int size = property.getAnnotation(Size.class).value();
			switch (size) {
			case Size.TIME_HH_MM: return 5;
			case Size.TIME_WITH_SECONDS: return 8;
			case Size.TIME_WITH_MILLIS: return 12;
			default: throw new IllegalArgumentException(String.valueOf(size));
			}
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

		@Override
		public void fillWithDemoData() {
			setObject(DateGenerator.generateRandomTime(size));
		}

	}

//	public static class JodaDateTimeField extends AbstractJodaField<LocalDateTime> {
//		
//		protected String getAllowedCharacters(PropertyInterface property) {
//			return "01234567890:.";
//		} 
//
//		protected int getAllowedSize(PropertyInterface property) {
//			int size = property.getAnnotation(Size.class).value();
//			switch (size) {
//			case Size.TIME_HH_MM: return 11 + 5;
//			case Size.TIME_WITH_SECONDS: return 11 + 8;
//			case Size.TIME_WITH_MILLIS: return 11 + 12;
//			default: throw new IllegalArgumentException(String.valueOf(size));
//			}
//		}
//		
//	}

		
}

package ch.openech.mj.edit.fields;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ch.openech.mj.autofill.DemoEnabled;
import ch.openech.mj.db.model.InvalidValues;
import ch.openech.mj.db.model.PropertyInterface;
import ch.openech.mj.model.annotation.LimitedString;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.util.DateUtils;
import ch.openech.mj.util.StringUtils;

/*

Ein Datumsfeld enthält die Angaben zu Tag, Monat und Jahr. Bei Geburtstagen kann der Monat und der Tag weggelassen werden. Die Eingabe wird
so gut wie möglich ergänzt. Die Übersetzung geschieht wie folgt:

 030607 wird zu 03.06.2007 also dem 3. Juni im Jahr 2010
 2.2.02 wird zu 02.02.2002 also dem 2. Februar 2002
 1.1.99 wird zu 01.01.1999 Zweistellige Jahreszahlen werden bis 20 als 20xx, ab 21 als 19xx interpretiert

 */

public class DateField extends AbstractEditField<LocalDate> implements DemoEnabled {
	public static final boolean PARTIAL_ALLOWED = true;                                                    
	
	private static final DateTimeFormatter US_MEDIUM_FORMAT = DateTimeFormat.forPattern(DateTimeFormat.patternForStyle("M-", Locale.US));
	
	private final TextField textField;
	
	private final boolean partialAllowed;
	
	public DateField(PropertyInterface property) {
		this(property, false);
	}
	
	public DateField(PropertyInterface property, boolean partialAllowed) {
		this(property, partialAllowed, true);
	}
	
	public DateField(PropertyInterface property, boolean partialAllowed, boolean editable) {
		super(property, editable);
		this.partialAllowed = partialAllowed;
		
		if (editable) {
			textField = ClientToolkit.getToolkit().createTextField(listener(), 10, "01234567890.");
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
				LocalDate value = getObject();
				if (value != null) {
					setObject(value);
				}
			}
		});
	}
	
	@Override
	public LocalDate getObject() {
		String text = textField.getText();
		if (text == null) return null;
		text = text.trim();
		if (text.length() == 0) return null;
		String textUS = DateUtils.parseCH(text, partialAllowed);
		try {
			return US_MEDIUM_FORMAT.parseLocalDate(textUS);
		} catch (IllegalArgumentException iae) {
			return InvalidValues.createInvalidLocalDate(text);
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

	public void setEnabled(boolean enabled) {
		textField.setEnabled(enabled);
		if (!enabled) {
			setObject(null);
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
		setObject(generateRandom());
	}

	public static LocalDate generateRandom() {
		int year =(int)(Math.random() * 80) + 1930;
		int month =(int)(Math.random() * 12) + 1;
		int day;
		if (month == 4 || month == 6 || month == 9 || month == 11) {
			day =(int)(Math.random() * 30) + 1;
		} else if (month == 2) {
			day =(int)(Math.random() * 28) + 1;
		} else {
			day =(int)(Math.random() * 31) + 1;
		}
		LocalDate localDate = new LocalDate(year, month, day);
		return localDate;
	}
	
//	@Override
//	public void validate(List<ValidationMessage> list) {
//		String value = getObject();
//		if (StringUtils.isBlank(textField.getText())) {
//			return;
//		}
//
//		if (!DateUtils.isValueValidUS(value, partialAllowed)) {
//			list.add(new ValidationMessage(getName(), "Fehlerhaftes Format des Datums"));
//			return;
//		}
//
//		boolean complete = value != null && value.length() == 10;
//		if (!partialAllowed && !complete) {
//			list.add(new ValidationMessage(getName(), "Komplettes Datum erforderlich"));
//			return;
//		}
//	}
	
	
}

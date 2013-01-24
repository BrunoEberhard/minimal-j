package ch.openech.mj.edit.fields;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Calendar;

import org.joda.time.LocalDate;

import ch.openech.mj.model.InvalidValues;


// Not used at the moment. Swing specific
public class DateKeyListener extends KeyAdapter {

	private DateField dateField;
	private static Calendar END;
	
	public DateKeyListener(DateField dateField) {
		this.dateField = dateField;
		if (END == null) {
			END = Calendar.getInstance();
			END.set(9999, 11, 31);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		LocalDate value = dateField.getObject();
		boolean valid = value != null && !InvalidValues.isInvalid(value);
		int amount = 0;
		
		if (!valid && e.getKeyCode() == KeyEvent.VK_SPACE) {
			setToday();
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) amount = 1;
		else if (e.getKeyCode() == KeyEvent.VK_UP) amount = -1;
	
		if (amount != 0 && valid) {
			if (e.isShiftDown()) changeYear(amount);
			else if (e.isAltDown() || e.isControlDown()) changeMonth(amount);
			else changeDay(amount);
		}
		
	}

	private void setToday() {
		dateField.setObject(new LocalDate());
	}
	
	private void changeYear(int amount) {
		LocalDate value = dateField.getObject();
		dateField.setObject(value.plusYears(amount));
	}

	private void changeMonth(int amount) {
		LocalDate value = dateField.getObject();
		dateField.setObject(value.plusMonths(amount));
	}

	private void changeDay(int amount) {
		LocalDate value = dateField.getObject();
		dateField.setObject(value.plusDays(amount));
	}

}

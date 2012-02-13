package ch.openech.mj.edit.fields;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Calendar;

import ch.openech.mj.util.DateUtils;
import ch.openech.mj.util.StringUtils;


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
		if (StringUtils.isBlank(dateField.getObject())) {
			if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_SPACE) {
				setToday();
			}
		} else {
			int field = Calendar.DATE;
			if (e.isShiftDown()) field = Calendar.YEAR;
			else if (e.isAltDown() || e.isControlDown()) field = Calendar.MONTH;
			
			int amount = 0;
			if (e.getKeyCode() == KeyEvent.VK_DOWN) amount = 1;
			else if (e.getKeyCode() == KeyEvent.VK_UP) amount = -1;

			if (amount != 0) calcDay(field, amount);
		}
		
	}

	private void setToday() {
		dateField.setObject(DateUtils.getToday());
	}

	private void calcDay(int field, int amount) {
		try {
			Calendar c1 = Calendar.getInstance(); 
			c1.setTime(DateUtils.dateFormatUS.parse(dateField.getObject()));
			c1.add(field, amount);
			if (c1.after(END)) return;
			String dateString = DateUtils.dateFormatUS.format(c1.getTime());
			dateField.setObject(dateString);
		} catch (Exception x) {
			// silent
		}
	}
	
}

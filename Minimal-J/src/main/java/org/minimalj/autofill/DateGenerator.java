package org.minimalj.autofill;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.minimalj.model.annotation.Size;

public class DateGenerator {

	public static LocalDate generateRandomDate() {
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

	
	public static LocalTime generateRandomTime(int size) {
		int hour =(int)(Math.random() * 24);
		int minute =(int)(Math.random() * 60);
		if (size == Size.TIME_HH_MM) {
			return new LocalTime(hour, minute);
		} else {
			int second =(int)(Math.random() * 60);
			if (size == Size.TIME_WITH_SECONDS) {
				return new LocalTime(hour, minute, second);
			} else if (size == Size.TIME_WITH_SECONDS) {
				int milis =(int)(Math.random() * 1000);
				return new LocalTime(hour, minute, second, milis);
			} else {
				throw new IllegalArgumentException(String.valueOf(size));
			}
		}
	}

}

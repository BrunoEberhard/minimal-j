package org.minimalj.util.mock;

import java.time.LocalDate;
import java.time.LocalTime;

import org.minimalj.model.annotation.Size;

public class MockDate {

	public static LocalDate generateRandomDate() {
		int year = (int) (Math.random() * 80) + 1930;
		int month = (int) (Math.random() * 12) + 1;
		int day;
		if (month == 4 || month == 6 || month == 9 || month == 11) {
			day = (int) (Math.random() * 30) + 1;
		} else if (month == 2) {
			day = (int) (Math.random() * 28) + 1;
		} else {
			day = (int) (Math.random() * 31) + 1;
		}
		LocalDate localDate = LocalDate.of(year, month, day);
		return localDate;
	}

	public static String generateRandomDatePartiallyKnown() {
		int year = (int) (Math.random() * 80) + 1930;
		if (Math.random() > 0.98)
			return "" + year;

		int month = (int) (Math.random() * 12) + 1;
		if (Math.random() > 0.98)
			return year + "-" + month;

		int day;
		if (month == 4 || month == 6 || month == 9 || month == 11) {
			day = (int) (Math.random() * 30) + 1;
		} else if (month == 2) {
			day = (int) (Math.random() * 28) + 1;
		} else {
			day = (int) (Math.random() * 31) + 1;
		}
		return year + "-" + month + "-" + day;
	}

	public static LocalTime generateRandomTime(int size) {
		int hour = (int) (Math.random() * 24);
		int minute = (int) (Math.random() * 60);
		if (size == Size.TIME_HH_MM) {
			return LocalTime.of(hour, minute);
		} else {
			int second = (int) (Math.random() * 60);
			if (size == Size.TIME_WITH_SECONDS) {
				return LocalTime.of(hour, minute, second);
			} else if (size == Size.TIME_WITH_SECONDS) {
				int milis = (int) (Math.random() * 1000);
				return LocalTime.of(hour, minute, second, milis);
			} else {
				throw new IllegalArgumentException(String.valueOf(size));
			}
		}
	}

}

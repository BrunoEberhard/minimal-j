package org.minimalj.autofill;

public class NumberGenerator {

	public static String generate(int minLength, int maxLength) {
		double add = 1;
		for (int i = 1; i<minLength; i++) {
			add = add * 10;
		}
		double multi = 1;
		for (int i = 0; i<maxLength && i < 16; i++) {
			multi = multi * 10;
		}
		double value = Math.random() * (multi - add) + add;
		long valueLong = (long)value;
		return Long.toString(valueLong);
	}
	
}

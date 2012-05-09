package ch.openech.mj.db.model;

import java.math.BigDecimal;
import java.util.Comparator;

public class NumberFormat implements Format {

	private final Class<?> clazz;
	private final boolean negative;
	private final int size;
	private final int decimalPlaces;

	public NumberFormat(Class<?> clazz, int size, boolean negative) {
		this(clazz, size, 0, negative);
	}
	
	public NumberFormat(Class<?> clazz, int size, int decimalPlaces, boolean negative) {
		if (clazz.equals(Integer.class) || clazz.equals(Long.class)) {
			if (decimalPlaces != 0) throw new IllegalArgumentException("Integer with decimal places not allowed");
		} else if (clazz.equals(BigDecimal.class)) {
			// nothing
		} else {
			throw new IllegalArgumentException("Only Integer, Long and Double as Number Classes allowed");
		}
		this.clazz = clazz;
		this.size = size;
		this.decimalPlaces = decimalPlaces;
		this.negative = negative;
	}

	@Override
	public Class<?> getClazz() {
		return clazz;
	}
	
	@Override
	public int getSize() {
		return size;
	}

	public boolean isNegative() {
		return negative;
	}
	
	public int getDecimalPlaces() {
		return decimalPlaces;
	}

	@Override
	public String display(String value) {
		return value;
	}

	@Override
	public String displayForEdit(String value) {
		return value;
	}
	
	public Comparator<String> getComparator() {
		if (clazz.equals(Integer.class)) {
			return new IntegerComparator();
		} else if (clazz.equals(Long.class)) {
			return new LongComparator();
		} else if (clazz.equals(BigDecimal.class)) {
			return new BigDecimalComparator();			
		} else {
			throw new IllegalStateException();
		}
	}
	
	private static class IntegerComparator implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			int v1 = value(o1);
			int i2 = value(o2);
			if (v1 == i2) return 0;
			return v1 < i2 ? -1 : 1;
		}
		
		private int value(String s) {
			if (s != null) {
				try {
					return Integer.parseInt(s);
				} catch (NumberFormatException nfe) {
					// do nothing, value is 0
					return 0;
				}	
			} else {
				return 0;
			}
		}
	}

	private static class LongComparator implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			long v1 = value(o1);
			long i2 = value(o2);
			if (v1 == i2) return 0;
			return v1 < i2 ? -1 : 1;
		}
		
		private long value(String s) {
			if (s != null) {
				try {
					return Long.parseLong(s);
				} catch (NumberFormatException nfe) {
					// do nothing, value is 0
					return 0;
				}	
			} else {
				return 0;
			}
		}
	}

	private static class BigDecimalComparator implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			BigDecimal v1 = value(o1);
			BigDecimal v2 = value(o2);
			return v1.compareTo(v2);
		}
		
		private BigDecimal value(String s) {
			if (s != null) {
				try {
					return new BigDecimal(s);
				} catch (NumberFormatException nfe) {
					// do nothing, value is 0
					return BigDecimal.ZERO;
				}	
			} else {
				return BigDecimal.ZERO;
			}
		}
	}

}

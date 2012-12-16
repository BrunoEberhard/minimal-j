package ch.openech.mj.db.model;

import java.lang.annotation.Annotation;

public @interface Number {

	boolean negative() default true;
	int precision();
	int digits();

	public static class NumberImpl implements Number {

		private boolean negative;
		private int precision;
		private int digits;

		public NumberImpl(int precision, int digits) {
			this(true, precision, digits);
		}

		public NumberImpl(boolean negative, int precision, int digits) {
			this.negative = negative;
			this.precision = precision;
			this.digits = digits;
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return Number.class;
		}

		@Override
		public boolean negative() {
			return negative;
		}

		@Override
		public int precision() {
			return precision;
		}

		@Override
		public int digits() {
			return digits;
		}
	
	}
//		if (clazz.equals(Integer.class) || clazz.equals(Long.class)) {
//			if (decimalPlaces != 0) throw new IllegalArgumentException("Integer with decimal places not allowed");
//		} else if (clazz.equals(BigDecimal.class)) {
//			// nothing
//		} else {
//			throw new IllegalArgumentException("Only Integer, Long and Double as Number Classes allowed");
//		}
//		this.clazz = clazz;
//		this.size = size;
//		this.decimalPlaces = decimalPlaces;
//		this.negative = negative;


}

package ch.openech.mj.db.model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Decimal {

	/**
	 * 
	 * @return size inclusive decimalPlaces but without a possible minus sign
	 */
	int size();
	int decimalPlaces() default 2;
	
	/**
	 * 
	 * @return true if negative values are allowed
	 */
	boolean negative() default false;
}

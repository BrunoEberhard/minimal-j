package ch.openech.mj.model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Number of decimal places for BigDecimal
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Decimal {

	int value();
	
}

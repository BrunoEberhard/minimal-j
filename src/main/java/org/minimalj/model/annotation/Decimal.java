package org.minimalj.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Number of decimal places for BigDecimal
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface Decimal {

	/**
	 * 
	 * @return the maximal decimal places persisted
	 */
	int value();

	/**
	 * {@link org.minimalj.model.Rendering#toString(Object)}
	 * 
	 * @return the minimal decimal places shown when the annotated BigDecimal is rendered
	 */
	int minDecimals() default Integer.MAX_VALUE;
}
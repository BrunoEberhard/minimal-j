package org.minimalj.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for String, Long, Integer, BigDecimal.
 * 
 * <UL>
 * <LI>For String its mandatory.
 * <LI>For Integer or Long the default size is defined by the size of the java values
 * <LI>For BigDecimal the default size is 10 (with scale of 0) as in MySql DB
 * </UL>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Size {

	int value();
	
	/**
	 * Constant to annotate the precision of LocalDate fields to minutes
	 */
	public static final int TIME_HH_MM = 4;

	/**
	 * Constant to annotate the precision of LocalDate fields to seconds
	 */
	public static final int TIME_WITH_SECONDS = 6;

	/**
	 * Constant to annotate the precision of LocalDate fields to milliseconds
	 */
	public static final int TIME_WITH_MILLIS = 9;
		
}

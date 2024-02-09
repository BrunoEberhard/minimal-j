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
 * <LI>For Integer or Long the default size is defined by the size of the java
 * values
 * <LI>For BigDecimal the default size is 10 (with scale of 0) as in MySql DB.
 * The size means the total count of digits including the decimals.
 * <LI>For Time the defined constants must be used
 * </UL>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Size {

	int value();
	
	/**
	 * Constant to annotate the precision of LocalDate fields to minutes
	 */
	public static final int TIME_HH_MM = 5;

	/**
	 * Constant to annotate the precision of LocalDate fields to seconds
	 */
	public static final int TIME_WITH_SECONDS = 8;

	/**
	 * Constant to annotate the precision of LocalDate fields to milliseconds
	 */
	public static final int TIME_WITH_MILLIS = 12;
	

	/**
	 * Maximum size of an Integer (unsigned)
	 */
	public static final int INTEGER = String.valueOf(Integer.MAX_VALUE).length();

	/**
	 * Maximum size of a Long (unsigned)
	 */
	public static final int LONG = String.valueOf(Long.MAX_VALUE).length();
	
	
	/**
	 * The default size (number of digits) for BigDecimals.
	 */
	public static final int BIG_DECIMAL_DEFAULT = 14;

}

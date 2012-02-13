package ch.openech.mj.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Just a marker annotation to easily find all parts of the source code
 * that implement a specific business rule
 * 
 */
@Retention(RetentionPolicy.SOURCE)
public @interface BusinessRule {

	String value();
	
}

package ch.openech.mj.model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a property to be a code. Codes must be registred at the
 * <code>Codes</code> class.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Code {

	String value() default "";
	
}

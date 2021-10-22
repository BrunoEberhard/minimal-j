package org.minimalj.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Of course only works for Html Frontends
 * 
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Attributes/autocomplete">autocomplete attribute in html</a> 
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface Autocomplete {

	public static final String ON = "on";

	public static final String OFF = "off";

	public static final String USERNAME = "username";

	public static final String CURRENT_PASSWORD = "current-password";

	String value() default ON;

	/**
	 * Framework internal
	 *
	 */
	public interface Autocompletable {
		
		public void setAutocomplete(String autocomplete);
	
	}
}

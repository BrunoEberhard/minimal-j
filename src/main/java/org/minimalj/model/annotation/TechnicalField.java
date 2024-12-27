package org.minimalj.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Note that the precision of these fields depend on the used repository or
 * database.
 * <p>
 * 
 * Technical fields are not validated by the method
 * org.minimalj.frontend.editor.Validator.validate(Object) as their values is
 * set not before writing to database.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface TechnicalField {

	public enum TechnicalFieldType {
		CREATE_USER, EDIT_USER, CREATE_DATE, EDIT_DATE;
	}
	
	TechnicalFieldType value();
	
}

package org.minimalj.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fields with these annotations are commonly used in database tables to provide
 * auditing capabilities and aid in debugging. They track a entity's lifecycle,
 * offering valuable insights into its creation and modification history. The
 * fields should not be used for any business cases.
 * <p>
 * 
 * The precision of these fields may depend on the used repository or database.
 * <p>
 * 
 * Technical fields are not validated by the method
 * org.minimalj.frontend.editor.Validator.validate(Object) as their values are
 * set not before writing to database.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface TechnicalField {

	public enum TechnicalFieldType {
		CREATE_USER, EDIT_USER, CREATE_DATE, EDIT_DATE;
	}

	TechnicalFieldType value();

}

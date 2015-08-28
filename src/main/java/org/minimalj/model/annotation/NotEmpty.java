package org.minimalj.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If a field is marked as NotEmpty it must not be null.
 * If it's a String field it also must not be empty.
 * If it's a Integer or Long field it must not be 0.<p>
 * 
 * In DB Persistence the column will be marked as <code>NOT NULL</code>.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NotEmpty {

}
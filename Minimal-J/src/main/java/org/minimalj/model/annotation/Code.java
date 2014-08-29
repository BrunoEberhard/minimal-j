package org.minimalj.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field to be a uninque code. This may be used by the persistence
 * instead of an special id field.
 * 
 * One, but only one, instance can have a <code>null</code> value
 * for this field
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Code {

}

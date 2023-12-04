package org.minimalj.model.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Normally only Codes are allowed to reference itself (without use of Views)
 * With this Annotation the ModelTest allows self references.
 * <p>
 * 
 * Be very careful with this annotation. Repositories may produce stake
 * overflows if entities have cycle references.
 * <p>
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface SelfReferenceAllowed {

}

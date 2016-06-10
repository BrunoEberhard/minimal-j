package org.minimalj.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.minimalj.model.Grant.Grants;

@Repeatable(Grants.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PACKAGE})
public @interface Grant {

	String[] value();

	Privilege privilege() default Privilege.ALL;

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE, ElementType.PACKAGE})
	public static @interface Grants {
		Grant[] value();
	}
	
	public static enum Privilege {
		SELECT, INSERT, UPDATE, DELETE, ALL;
	}
}

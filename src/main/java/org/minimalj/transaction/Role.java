package org.minimalj.transaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.minimalj.transaction.Role.Roles;

@Repeatable(Roles.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PACKAGE})
public @interface Role {

	String[] value();

	@SuppressWarnings("rawtypes")
	Class<? extends Transaction> transaction() default Transaction.class;

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE, ElementType.PACKAGE})
	public static @interface Roles {
		Role[] value();
	}
}

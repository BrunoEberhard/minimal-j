package org.minimalj.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE, ElementType.METHOD })
public @interface Width {

	int width();

	int maxWidth() default -1;

	public static final int DEFAULT = 100;

	public static final int SMALLEST = 50;
	public static final int SMALLER = 70;
	public static final int LARGER = 150;
	public static final int LARGEST = 250;

}
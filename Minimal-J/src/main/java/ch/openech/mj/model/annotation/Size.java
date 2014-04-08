package ch.openech.mj.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Size {

	int value();
	
	public static final int TIME_HH_MM = 4;
	public static final int TIME_WITH_SECONDS = 6;
	public static final int TIME_WITH_MILLIS = 9;
		
}

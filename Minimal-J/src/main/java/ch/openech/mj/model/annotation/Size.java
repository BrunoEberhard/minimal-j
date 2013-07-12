package ch.openech.mj.model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Size {

	int value();
	
	public static final int TIME_HH_MM = 4;
	public static final int TIME_WITH_SECONDS = 6;
	public static final int TIME_WITH_MILLIS = 9;
		
}

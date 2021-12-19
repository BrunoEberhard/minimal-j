package org.minimalj.model;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.minimalj.frontend.impl.util.ColumnFilter;

@Retention(RUNTIME)
@Target(METHOD)
public @interface TableFilter {

	Class<? extends ColumnFilter> value();
	
}

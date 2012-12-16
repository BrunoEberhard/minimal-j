package ch.openech.mj.edit.value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If a field is marked as required it may not be null.<p>
 * 
 * In DB Persistence the column will be marked as <code>NOT NULL</code>.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Required {

}
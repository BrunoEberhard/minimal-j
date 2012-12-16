package ch.openech.mj.db.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Somehow the common interface of a public field and
 * a getter/setter pair
 * 
 * @author bruno
 */
public interface PropertyInterface {

	public Class<?> getDeclaringClass();
	
	public String getFieldName();

	public Class<?> getFieldClazz();

	public Type getType();

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass);

	public Object getValue(Object object);

	public void setValue(Object object, Object value);
	
	public boolean isFinal();
}
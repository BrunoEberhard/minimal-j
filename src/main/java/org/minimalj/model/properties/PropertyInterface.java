package org.minimalj.model.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Properties can be public fields or getter/setter pairs. This interface is intended
 * for internal Minimal-J use. Sometimes it's also usefull for an actual application but
 * you can use Minimal-J without furthur knowledge of this interface and its implementations.<p>
 * 
 * Some properties can be chained. You can as the Keys class for a Property "propertyA.propertyB".
 * This means you get the propertyB of the class used for propertyA. This chains are the
 * reason for the getPath method in this interface. For all other method the last part
 * of the chain is used for ChainedProperties.
 * 
 */
public interface PropertyInterface {

	public Class<?> getDeclaringClass();
	
	public String getName();

	public String getPath();

	public Class<?> getClazz();

	public Type getType();

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass);

	public Object getValue(Object object);

	public void setValue(Object object, Object value);
	
	public boolean isFinal();
}
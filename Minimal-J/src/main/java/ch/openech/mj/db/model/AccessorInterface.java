package ch.openech.mj.db.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Somehow the common interface of a public field and
 * a getter/setter pair
 * 
 * @author bruno
 */

// DataField, ModelField, ValueField, Property, PropertyField ??
public interface AccessorInterface {

	public String getName();

	public Class<?> getClazz();

	// TODO ..
//	Nur noch getType anbieten (getClazz, getA streichen), welches dann aber auch z.B.
//	SizedType zurückgeben kann, womit die Länge des Feldes definiert wird
	
	// Jedoch was nützt das, Type ist ein leeres Interface und es wird kaum je direkt
	// eine Klasse zurückgegeben
	
	public Type getType();

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass);

	public Object getValue(Object object);

	public void setValue(Object object, Object value);
	
	public boolean isFinal();
}
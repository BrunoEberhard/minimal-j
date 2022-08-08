package org.minimalj.frontend.form.element;

import java.lang.annotation.Annotation;

import org.minimalj.model.annotation.Size;
import org.minimalj.model.properties.VirtualProperty;

/**
 * Produces an empty padding field in forms. Please don't overuse this or
 * try to be clever on layouting.
 * 
 */
public class EmptyFormElement extends TextFormElement {

	private static final EmptyProperty EMPTY_PROPERTY = new EmptyProperty();
	
	public EmptyFormElement() {
		super(EMPTY_PROPERTY);
	}
	
	@Override
	public String getCaption() {
		return null;
	}
	
	private static class EmptyProperty extends VirtualProperty implements Size {
		
		@Override
		public String getName() {
			return ""; // null could cause NPE
		}

		@Override
		public Class<?> getClazz() {
			return null;
		}

		@Override
		public Object getValue(Object object) {
			return null;
		}

		@Override
		public void setValue(Object object, Object value) {
			// ignored
		}
		
		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			if (annotationClass == Size.class) {
				return (T) this;
			}
			return super.getAnnotation(annotationClass);
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return null;
		}

		@Override
		public int value() {
			return 0;
		}
	}
}

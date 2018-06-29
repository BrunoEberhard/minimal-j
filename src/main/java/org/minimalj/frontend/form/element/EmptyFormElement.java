package org.minimalj.frontend.form.element;

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
	
	private static class EmptyProperty extends VirtualProperty {
		
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
	}
}

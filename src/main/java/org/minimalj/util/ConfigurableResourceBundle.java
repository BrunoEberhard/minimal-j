package org.minimalj.util;

import java.util.Enumeration;
import java.util.ResourceBundle;

import org.minimalj.model.Keys;

public class ConfigurableResourceBundle extends ResourceBundle {

	@Override
	protected Object handleGetObject(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	public static class Resource {
		
		public static final Resource $ = Keys.of(Resource.class);
		
		public String key;
		
		public String value;
	}

	
}

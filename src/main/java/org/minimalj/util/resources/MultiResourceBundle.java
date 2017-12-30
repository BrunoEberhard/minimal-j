package org.minimalj.util.resources;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class MultiResourceBundle extends ResourceBundle {

	private final List<ResourceBundle> resourceBundles = new ArrayList<>();
	
	public MultiResourceBundle(ResourceBundle... resourceBundles) {
		for (ResourceBundle resourceBundle : resourceBundles) {
			addResourceBundle(resourceBundle);
		}
	}
	
	public MultiResourceBundle(Locale locale, String... resourceBundleNames) {
		for (String resourceBundleName : resourceBundleNames) {
			addResourceBundle(ResourceBundle.getBundle(resourceBundleName, locale, Control.getNoFallbackControl(Control.FORMAT_PROPERTIES)));
		}
	}
	
	public MultiResourceBundle(List<ResourceBundle> resourceBundles) {
		for (ResourceBundle resourceBundle : resourceBundles) {
			addResourceBundle(resourceBundle);
		}
	}

	private void addResourceBundle(ResourceBundle resourceBundle) {
		if (resourceBundle instanceof MultiResourceBundle) {
			MultiResourceBundle multiResourceBundle = (MultiResourceBundle) resourceBundle;
			for (ResourceBundle innerBundle : multiResourceBundle.resourceBundles) {
				addResourceBundle(innerBundle);
			}
		} else {
			resourceBundles.add(resourceBundle);
		}
	}
	
	@Override
	public Enumeration<String> getKeys() {
		LinkedHashSet<String> resultKeys = new LinkedHashSet<String>();
		for (ResourceBundle resourceBundle : resourceBundles) {
			Enumeration<String> keys = resourceBundle.getKeys();
			while (keys.hasMoreElements()) {
				resultKeys.add(keys.nextElement());
			}
		}
		return new IteratorEnumeration<String>(resultKeys.iterator());
	}

	@Override
	public boolean containsKey(String key) {
		for (ResourceBundle resourceBundle : resourceBundles) {
			if (resourceBundle.containsKey(key)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected Object handleGetObject(String key) {
		Object result = null;
		for (ResourceBundle resourceBundle : resourceBundles) {
			try {
				result = resourceBundle.getObject(key);
			} catch (Exception x) {
				// silent
			}
			if (result != null) break;
		}
		return result;
	}

	private class IteratorEnumeration<T> implements Enumeration<T> {
		private Iterator<T> iterator;

		public IteratorEnumeration(Iterator<T> iterator) {
			this.iterator = iterator;
		}

		@Override
		public boolean hasMoreElements() {
			return iterator.hasNext();
		}

		@Override
		public T nextElement() {
			return iterator.next();
		}
	}
	
}

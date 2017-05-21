package org.minimalj.util.resources;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;

public class MultiResourceBundle extends ResourceBundle {

	private List<ResourceBundle> resourceBundles;
	
	public MultiResourceBundle(ResourceBundle... resourceBundles) {
		this(Arrays.asList(resourceBundles));
	}
	
	public MultiResourceBundle(List<ResourceBundle> resourceBundles) {
		this.resourceBundles = resourceBundles;
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

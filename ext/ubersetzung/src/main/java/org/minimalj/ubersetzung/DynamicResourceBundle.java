package org.minimalj.ubersetzung;

import java.util.Enumeration;
import java.util.ResourceBundle;

import org.minimalj.ubersetzung.model.Ubersetzung;
import org.minimalj.ubersetzung.model.UbersetzungEntry;

public class DynamicResourceBundle extends ResourceBundle {

	private final Ubersetzung ubersetzung;
	
	public DynamicResourceBundle(Ubersetzung ubersetzung) {
		this.ubersetzung = ubersetzung;
	}
	
	@Override
	protected Object handleGetObject(String key) {
		for (UbersetzungEntry entry : ubersetzung.entries) {
			if (entry.key.equals(key)) {
				return entry.value;
			}
		}
		return null;
	}

	@Override
	public Enumeration<String> getKeys() {
		return new Enumeration<String>() {
			private int i = 0;

			@Override
			public boolean hasMoreElements() {
				return i < ubersetzung.entries.size();
			}

			@Override
			public String nextElement() {
				return ubersetzung.entries.get(i++).key;
			}
		};
	}
}

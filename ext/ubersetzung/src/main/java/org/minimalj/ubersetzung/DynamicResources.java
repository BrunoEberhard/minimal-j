package org.minimalj.ubersetzung;

import java.util.List;
import java.util.Locale;

import org.minimalj.backend.Backend;
import org.minimalj.repository.query.By;
import org.minimalj.ubersetzung.model.Ubersetzung;

public class DynamicResources {

	public static DynamicResourceBundle getResourceBundle(Locale locale) {
		List<Ubersetzung> ubersetzungs = Backend.find(Ubersetzung.class, By.field(Ubersetzung.$.lang, locale.getLanguage()).and(By.field(Ubersetzung.$.country, locale.getCountry())));
		if (!ubersetzungs.isEmpty()) {
			return new DynamicResourceBundle(ubersetzungs.get(0));
		}
		ubersetzungs = Backend.find(Ubersetzung.class, By.field(Ubersetzung.$.lang, locale.getLanguage()));
		if (!ubersetzungs.isEmpty()) {
			return new DynamicResourceBundle(ubersetzungs.get(0));
		}
		return null;
	}
}

package org.minimalj.example.miniboost.frontend.element;

import java.util.List;
import java.util.stream.Collectors;

import org.minimalj.example.miniboost.model.Country;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.form.element.AbstractFormElement;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.Property;
import org.minimalj.util.Codes;
import org.minimalj.util.mock.Mocking;

public class CountryFormElement extends AbstractFormElement<Country> implements Mocking {
	private final Input<Country> lookup;
	private final List<Country> countries;
	
	public CountryFormElement(Object key) {
		this(Keys.getProperty(key));
	}
	
	public CountryFormElement(Property property) {
		super(property);
		countries = Codes.get(Country.class);
		lookup = Frontend.getInstance().createLookup(listener(), new CountrySearch(), new Object[] {Country.$.nameDe, Country.$.id});
	}
	
	private class CountrySearch implements Search<Country> {

		@Override
		public List<Country> search(String searchText) {
			return countries.stream().filter(c -> {
				return c.nameDe.contains(searchText) || c.code2.contains(searchText);
 			}).collect(Collectors.toList());
		}
	}

	
	@Override
	public IComponent getComponent() {
		return lookup;
	}
	
	@Override
	public Country getValue() {
		return lookup.getValue();
	}

	@Override
	public void setValue(Country country) {
		lookup.setValue(country);
	}

	@Override
	public void mock() {
		if (!countries.isEmpty()) {
			int index = (int) (Math.random() * countries.size());
			setValue(countries.get(index));
		}
	}

}
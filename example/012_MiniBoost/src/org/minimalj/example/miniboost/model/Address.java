package org.minimalj.example.miniboost.model;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.util.StringUtils;

public class Address {

	@Size(50)
	public String street, city;

	@Size(20)
	public String zip;
	
	public Country country;
	
	public String getCountryAndZip() {
		if (Keys.isKeyObject(this)) return Keys.methodOf(this, "countryAndZip");

		if (!StringUtils.isBlank(country.id)) {
			if (!StringUtils.isBlank(zip)) {
				return country + " " + zip;
			} else {
				return country.id;
			}
		} else {
			return zip;
		}
	}

}

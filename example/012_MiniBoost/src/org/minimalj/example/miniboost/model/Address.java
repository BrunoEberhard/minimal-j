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

		if (country.id != null) {
			if (!StringUtils.isBlank(zip)) {
				return country.code2 + " " + zip;
			} else {
				return country.code2;
			}
		} else {
			return zip;
		}
	}

}

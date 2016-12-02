package org.minimalj.example.miniboost.model;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.util.StringUtils;

public class Address {

	@Size(50)
	public String street, city;

	@Size(20)
	public String zip;
	
	@Size(2) // TODO reference Country
	public String country;
	
	public String getCountryAndZip() {
		if (Keys.isKeyObject(this)) return Keys.methodOf(this, "countryAndZip", String.class);

		if (!StringUtils.isBlank(country)) {
			if (!StringUtils.isBlank(zip)) {
				return country + " " + zip;
			} else {
				return country;
			}
		} else {
			return zip;
		}
	}

}

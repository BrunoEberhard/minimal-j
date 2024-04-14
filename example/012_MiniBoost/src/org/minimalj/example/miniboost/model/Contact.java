package org.minimalj.example.miniboost.model;

import org.minimalj.model.annotation.Size;

public class Contact {

	@Size(20)
	public String phone1, phone2, fax;

	@Size(100)
	public String email;

}

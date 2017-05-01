package org.minimalj.example.minimail.model;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class EMailAddress {
	public static final EMailAddress $ = Keys.of(EMailAddress.class);

	@Size(2000)
	public String address;
}

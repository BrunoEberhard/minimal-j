package org.minimalj.example.ebanking.model;

import java.math.BigDecimal;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Size;

public class Account {

	public static final Account $ = Keys.of(Account.class);
	
	public Object id;
	
	@Size(32) @NotEmpty
	public String accountNr;
	
	@Size(255) @NotEmpty
	public String description;
	
	public BigDecimal value;
}

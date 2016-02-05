package org.minimalj.example.ebanking.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Decimal;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.annotation.Size;

public class AccountPosition {

	public static final AccountPosition $ = Keys.of(AccountPosition.class);
	
	public Object id;
	
	@NotEmpty
	public Account account;
	
	@NotEmpty @Decimal(2)
	public BigDecimal amount;
	
	@NotEmpty
	public LocalDate valueDate;
	
	@Size(255) @NotEmpty @Searched
	public String description;
}

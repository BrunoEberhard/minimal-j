package org.minimalj.example.currencies.model;

import java.time.LocalDate;

import org.minimalj.model.Code;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

/**
 * The iso currencies list is not normalized.
 * Currencies are repeated for every country that
 * uses a currency.
 */
public class Currency implements Code {
	public static final Currency $ = Keys.of(Currency.class);
	
	@Size(3)
	public String id;
	
	@Size(140)
	public String name;

	public Boolean fund;
	
	@Size(3)
	public Integer number;
	
	@Size(1)
	public Integer minorUnits;
	
	public LocalDate withdrawalDate;
}

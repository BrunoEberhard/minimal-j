package org.minimalj.example.currencies.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.minimalj.model.Keys;

public class Currency {
	public static final Currency $ = Keys.of(Currency.class);
	
	public String name;
	public BigDecimal value;
	public LocalDate date;

}

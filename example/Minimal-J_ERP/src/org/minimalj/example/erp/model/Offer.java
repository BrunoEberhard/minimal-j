package org.minimalj.example.erp.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class Offer {

	public static final Offer $ = Keys.of(Offer.class);
	
	public Object id;

	@Size(32)
	public String offerNr;

	@Size(32)
	public String title;
	
	public LocalDate creationdate, validuntil;

	public CustomerView customer;
	
	public final List<OfferArticle> articles = new ArrayList<>();

	@Size(10)
	public BigDecimal totalPrice, grosstotalprice;
	
	@Size(4)
	public BigDecimal tax_in_percent, discount_in_percent;

	public Boolean fixprice;
	
	public Boolean isordered;
}

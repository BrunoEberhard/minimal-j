package org.minimalj.example.erp.model;

import java.math.BigDecimal;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class OfferArticle {

	public static final OfferArticle $ = Keys.of(OfferArticle.class);
	
	public ArticleView article;
	@Size(3)
	public Integer numberof;
	
	@Size(10)
	public BigDecimal price;
}

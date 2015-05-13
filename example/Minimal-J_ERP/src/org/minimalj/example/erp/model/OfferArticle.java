package org.minimalj.example.erp.model;

import java.math.BigDecimal;
import java.util.Locale;

import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.annotation.Size;

public class OfferArticle implements Rendering {

	public static final OfferArticle $ = Keys.of(OfferArticle.class);
	
	public ArticleView article;
	@Size(3)
	public Integer numberof;
	
	@Size(10)
	public BigDecimal price;

	@Override
	public String render(RenderType renderType, Locale locale) {
		StringBuilder s = new StringBuilder();
		s.append(article.article);
		if (numberof != null) {
			s.append(",  ").append(numberof).append(" Stück");
		}
		if (price != null) {
			s.append(",  Preis: ").append(price);
		}
		return s.toString();
	}
}

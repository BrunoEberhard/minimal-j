package org.minimalj.example.erp.model;

import java.math.BigDecimal;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.annotation.Size;

public class Article {

	public static final Article $ = Keys.of(Article.class);
	
	public Object id;
	
	@Size(32) @Searched
	public String article_nr;
	
	@Size(50) @Searched
	public String article;
	
	@Size(2000) @NotEmpty
	public String description;
	
	public ArticleCategory articleCategory;
	
	@Size(10)
	public BigDecimal price;
	public Unit unit;
	public Currency currency;
	
}

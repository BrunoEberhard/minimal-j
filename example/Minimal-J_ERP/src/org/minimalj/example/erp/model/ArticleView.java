package org.minimalj.example.erp.model;

import org.minimalj.model.Keys;
import org.minimalj.model.View;

public class ArticleView implements View<Article> {

	public static final ArticleView $ = Keys.of(ArticleView.class);

	public Object id;

	public String article_nr;
	
	public String article;
	
	public String description;
	
	public String display() {
		return article;
	}
}

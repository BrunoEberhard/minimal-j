package org.minimalj.example.erp.model;

import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.View;

public class ArticleView implements View<Article>, Rendering {

	public static final ArticleView $ = Keys.of(ArticleView.class);

	public Object id;

	public String articleNr;
	
	public String article;
	
	public String description;
	
	@Override
	public String render(RenderType renderType) {
		return article;
	}
}

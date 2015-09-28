package org.minimalj.example.erp.frontend.page;

import static org.minimalj.example.erp.model.Article.*;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.model.Article;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.SearchPage.SimpleSearchPage;
import org.minimalj.transaction.predicate.By;


public class ArticleSearchPage extends SimpleSearchPage<Article> {

	public static final Object[] FIELDS = {
		$.articleNr, //
		$.article, //
		$.price, //
	};
	
	public ArticleSearchPage(String query) {
		super(query, FIELDS);
	}
	
	@Override
	protected List<Article> load(String query) {
		return Backend.persistence().read(Article.class, By.search(query), 100);
	}

	@Override
	public ObjectPage<Article> createDetailPage(Article initialObject) {
		return new ArticlePage(initialObject);
	}
}
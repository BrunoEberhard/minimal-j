package org.minimalj.example.erp.frontend.page;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.model.Article;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.TableDetailPage;
import org.minimalj.repository.query.By;


public class ArticleTablePage extends TableDetailPage<Article> {

	public ArticleTablePage() {
		super(ArticleSearchPage.FIELDS);
	}
	
	@Override
	protected List<Article> load() {
		return Backend.find(Article.class, By.limit(100));
	}

	@Override
	protected ObjectPage<Article> getDetailPage(Article customer) {
		return new ArticlePage(customer);
	}

}

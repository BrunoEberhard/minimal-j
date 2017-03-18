package org.minimalj.example.erp.frontend.page;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.model.Article;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.TablePage.SimpleTablePageWithDetail;
import org.minimalj.repository.query.By;


public class ArticleTablePage extends SimpleTablePageWithDetail<Article> {

	public ArticleTablePage() {
		super(ArticleSearchPage.FIELDS);
	}
	
	@Override
	protected List<Article> load() {
		return Backend.find(Article.class, By.limit(100));
	}

	@Override
	protected ObjectPage<Article> createDetailPage(Article customer) {
		return new ArticlePage(customer);
	}

}

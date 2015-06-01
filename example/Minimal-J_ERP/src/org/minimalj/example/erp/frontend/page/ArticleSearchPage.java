package org.minimalj.example.erp.frontend.page;

import static org.minimalj.example.erp.model.Article.*;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.model.Article;
import org.minimalj.frontend.page.AbstractSearchPage;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.transaction.criteria.Criteria;


public class ArticleSearchPage extends AbstractSearchPage<Article> {

	public static final Object[] FIELDS = {
		$.article_nr, //
		$.article, //
		$.price, //
	};
	
	public ArticleSearchPage() {
		super(FIELDS);
	}
	
	@Override
	protected List<Article> load(String query) {
		return Backend.getInstance().read(Article.class, Criteria.search(query), 100);
	}

	@Override
	public void action(Article selectedObject) {
		ClientToolkit.getToolkit().show(new ArticlePage(selectedObject));
	}

}
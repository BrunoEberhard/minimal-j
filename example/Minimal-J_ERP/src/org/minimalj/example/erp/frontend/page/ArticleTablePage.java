package org.minimalj.example.erp.frontend.page;

import static org.minimalj.example.erp.model.Article.*;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.model.Article;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.transaction.criteria.Criteria;
import org.minimalj.util.IdUtils;


public class ArticleTablePage extends TablePage<Article> {

	private final String text;
	
	public static final Object[] FIELDS = {
		$.article_nr, //
		$.article, //
		$.price, //
	};
	
	public ArticleTablePage(String text) {
		super(FIELDS, text);
		this.text = text;
	}
	
	@Override
	protected List<Article> load(String searchText) {
		return Backend.getInstance().read(Article.class, Criteria.search(searchText), 100);
	}

	@Override
	protected void clicked(Article selectedObject, List<Article> selectedObjects) {
		show(ArticlePage.class, IdUtils.getIdString(selectedObject));
	}
	@Override
	public String getTitle() {
		return text;
	}

}

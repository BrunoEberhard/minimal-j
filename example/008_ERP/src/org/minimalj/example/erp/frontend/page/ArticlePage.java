package org.minimalj.example.erp.frontend.page;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.frontend.form.ArticleForm;
import org.minimalj.example.erp.model.Article;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.ObjectPage;

public class ArticlePage extends ObjectPage<Article> {

	public ArticlePage(Article article) {
		super(article);
	}
	
	public ArticlePage(Object id) {
		super(Article.class, id);
	}
	
	@Override
	public List<Action> getActions() {
		return null;
	}

	@Override
	protected Form<Article> createForm() {
		return new ArticleForm(false);
	}

	public class ArticleEditorAction extends ObjectEditor {

		@Override
		protected Form<Article> createForm() {
			return new ArticleForm(true);
		}

		@Override
		protected Article save(Article article) {
			return Backend.save(article);
		}
	}
}

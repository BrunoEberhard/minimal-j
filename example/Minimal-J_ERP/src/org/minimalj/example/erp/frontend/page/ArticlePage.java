package org.minimalj.example.erp.frontend.page;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.frontend.form.ArticleForm;
import org.minimalj.example.erp.model.Article;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.ObjectPage;

public class ArticlePage extends ObjectPage<Article> {

	public ArticlePage(Article article) {
		super(article);
	}
	
	@Override
	public ActionGroup getMenu() {
		ActionGroup menu = new ActionGroup("Article");
		menu.add(new ArticleEditorAction());
		return menu;
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
			return Backend.getInstance().update(article);
		}
	}
}

package org.minimalj.example.erp.frontend.page;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.frontend.editor.ArticleEditor;
import org.minimalj.example.erp.frontend.form.ArticleForm;
import org.minimalj.example.erp.model.Article;
import org.minimalj.frontend.edit.form.Form;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.ObjectPage;

public class ArticlePage extends ObjectPage<Article> {

	private final String articleId;

	public ArticlePage(String articleId) {
		this.articleId = articleId;
	}
	
	@Override
	protected Article loadObject() {
		return Backend.getInstance().read(Article.class, articleId);
	}
	
	@Override
	public ActionGroup getMenu() {
		ActionGroup menu = new ActionGroup("Article");
		menu.add(new ArticleEditor(getObject()));
		return menu;

	}

	@Override
	protected Form<Article> createForm() {
		return new ArticleForm(false);
	}

}

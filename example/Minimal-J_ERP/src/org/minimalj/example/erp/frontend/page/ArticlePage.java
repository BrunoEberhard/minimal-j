package org.minimalj.example.erp.frontend.page;

import org.minimalj.example.erp.frontend.editor.ArticleEditor;
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
		menu.add(new ArticleEditor(getObject()));
		return menu;
	}

	@Override
	protected Form<Article> createForm() {
		return new ArticleForm(false);
	}

}

package org.minimalj.example.erp.frontend.editor;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.frontend.form.ArticleForm;
import org.minimalj.example.erp.frontend.page.ArticlePage;
import org.minimalj.example.erp.model.Article;
import org.minimalj.frontend.edit.Editor;
import org.minimalj.frontend.edit.form.Form;
import org.minimalj.frontend.page.PageLink;

public class AddArticleEditor extends Editor<Article> {

	@Override
	protected Form<Article> createForm() {
		return new ArticleForm(true);
	}

	@Override
	protected String save(Article article) throws Exception {
		Object id = Backend.getInstance().insert(article);
		return PageLink.link(ArticlePage.class, id.toString());
	}

}

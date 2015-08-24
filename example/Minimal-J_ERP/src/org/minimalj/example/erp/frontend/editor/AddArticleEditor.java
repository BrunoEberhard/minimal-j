package org.minimalj.example.erp.frontend.editor;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.frontend.form.ArticleForm;
import org.minimalj.example.erp.frontend.page.ArticlePage;
import org.minimalj.example.erp.model.Article;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;

public class AddArticleEditor extends NewObjectEditor<Article> {

	@Override
	protected Form<Article> createForm() {
		return new ArticleForm(true);
	}

	@Override
	protected Object save(Article article) {
		return Backend.persistence().insert(article);
	}

	@Override
	protected void finished(Object newId) {
		Frontend.getBrowser().show(new ArticlePage(newId));
	}
}

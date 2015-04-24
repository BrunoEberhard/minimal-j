package org.minimalj.example.erp.frontend.editor;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.frontend.form.ArticleForm;
import org.minimalj.example.erp.frontend.page.ArticlePage;
import org.minimalj.example.erp.model.Article;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.Page;

public class AddArticleEditor extends Editor<Article> {

	@Override
	protected Form<Article> createForm() {
		return new ArticleForm(true);
	}

	@Override
	protected Page save(Article article) throws Exception {
		Article savedArticle = Backend.getInstance().insert(article);
		return new ArticlePage(savedArticle);
	}

}

package org.minimalj.example.erp.frontend.editor;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.frontend.form.ArticleForm;
import org.minimalj.example.erp.frontend.page.ArticlePage;
import org.minimalj.example.erp.model.Article;
import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.toolkit.ClientToolkit;

public class ArticleEditor extends SimpleEditor<Article> {

	private final Article article;
	
	public ArticleEditor(Article article) {
		this.article = article;
	}
	
	@Override
	protected Article createObject() {
		return article;
	}

	@Override
	protected Form<Article> createForm() {
		return new ArticleForm(true);
	}

	@Override
	protected Article save(Article article) {
		Article updatedArticle = Backend.getInstance().update(article);
		return updatedArticle;
	}
	
    @Override
    protected void finished(Article result) {
    	ClientToolkit.getToolkit().show(new ArticlePage(result));
    }


}

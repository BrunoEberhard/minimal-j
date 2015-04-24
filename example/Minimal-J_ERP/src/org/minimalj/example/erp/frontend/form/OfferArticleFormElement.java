package org.minimalj.example.erp.frontend.form;

import java.util.List;

import org.minimalj.example.erp.model.Article;
import org.minimalj.example.erp.model.OfferArticle;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.ObjectPanelFormElement;
import org.minimalj.frontend.form.element.ReferenceFormElement;
import org.minimalj.frontend.toolkit.Action;
import org.minimalj.model.Keys;

public class OfferArticleFormElement extends ObjectPanelFormElement<List<OfferArticle>> {

	public OfferArticleFormElement(List<OfferArticle> key, boolean editable) {
		super(Keys.getProperty(key), editable);
	}

	@Override
	protected Form<List<OfferArticle>> createFormPanel() {
		return null;
	}

	@Override
	protected void show(List<OfferArticle> offerArticles) {
		for (OfferArticle offerArticle : offerArticles) {
			addText(offerArticle.article.description);
			addGap();
		}
	}
	
	@Override
	protected void showActions() {
		addAction(new AddOfferArticleEditor());
		addAction(new RemoveOfferArticlesAction());
	}

	public class AddOfferArticleEditor extends ObjectFieldPartEditor<OfferArticle> {
		@Override
		public Form<OfferArticle> createForm() {
			Form<OfferArticle> form = new Form<>();
			form.line(new ReferenceFormElement<Article>(OfferArticle.$.article, Article.$.article, Article.$.article_nr));
			form.line(OfferArticle.$.numberof);
			form.line(OfferArticle.$.price);
			return form;
		}

		@Override
		protected OfferArticle getPart(List<OfferArticle> object) {
			return new OfferArticle();
		}

		@Override
		protected void setPart(List<OfferArticle> object, OfferArticle p) {
			object.add(p);
		}
	}


	public class RemoveOfferArticlesAction extends Action {

		@Override
		public void action() {
			getObject().clear();
			fireObjectChange();
		}
	}

}

package org.minimalj.example.erp.frontend.form;

import java.util.List;

import org.minimalj.example.erp.model.Article;
import org.minimalj.example.erp.model.OfferArticle;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.ListFormElement;
import org.minimalj.frontend.form.element.LookupFormElement;
import org.minimalj.model.Keys;

public class OfferArticleFormElement extends ListFormElement<OfferArticle> {

	public OfferArticleFormElement(List<OfferArticle> key, boolean editable) {
		super(Keys.getProperty(key), editable);
	}

	@Override
	protected Form<OfferArticle> createForm(boolean newObject) {
		Form<OfferArticle> form = new Form<>();
		form.line(new LookupFormElement<>(OfferArticle.$.article, Article.$.article, Article.$.articleNr));
		form.line(OfferArticle.$.numberof);
		form.line(OfferArticle.$.price);
		return form;
	}
	
	@Override
	protected Action[] getActions() {
		return new Action[] { new AddListEntryEditor(), new RemoveOfferArticlesAction() };
	}

	public class RemoveOfferArticlesAction extends Action {

		@Override
		public void run() {
			getValue().clear();
			handleChange();
		}
	}

}

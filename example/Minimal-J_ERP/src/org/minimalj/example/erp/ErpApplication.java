package org.minimalj.example.erp;

import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.example.erp.frontend.editor.AddArticleEditor;
import org.minimalj.example.erp.frontend.editor.AddCustomerEditor;
import org.minimalj.example.erp.frontend.page.ArticleTablePage;
import org.minimalj.example.erp.frontend.page.CustomerTablePage;
import org.minimalj.example.erp.model.Article;
import org.minimalj.example.erp.model.Customer;
import org.minimalj.example.erp.model.Offer;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.PageLink;
import org.minimalj.frontend.toolkit.IAction;
import org.minimalj.model.test.ModelTest;

public class ErpApplication extends Application {

	public ErpApplication() {
	}

	@Override
	public List<IAction> getActionsNew() {
		ActionGroup menu = new ActionGroup(null);
		menu.add(new AddCustomerEditor());
		menu.add(new AddArticleEditor());
		return menu.getItems();
	}	
	
	@Override
	public List<IAction> getActionsView() {
		ActionGroup menu = new ActionGroup(null);
		menu.add(new PageLink(CustomerTablePage.class));
		return menu.getItems();
	}

	@Override
	public Class<?>[] getSearchClasses() {
		return new Class<?>[] { ArticleTablePage.class, CustomerTablePage.class };
	}

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[]{Customer.class, Article.class, Offer.class};
	}

	public static void main(String[] args) {
		ModelTest test = new ModelTest(new ErpApplication().getEntityClasses());
		if (!test.getProblems().isEmpty()) {
			for (String s : test.getProblems()) {
				System.err.println(s);
			}
		}
	}
}

package org.minimalj.example.erp;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.example.erp.frontend.editor.AddArticleEditor;
import org.minimalj.example.erp.frontend.editor.AddCustomerEditor;
import org.minimalj.example.erp.frontend.page.ArticleSearchPage;
import org.minimalj.example.erp.frontend.page.ArticleTablePage;
import org.minimalj.example.erp.frontend.page.CustomerSearchPage;
import org.minimalj.example.erp.frontend.page.CustomerTablePage;
import org.minimalj.example.erp.model.Article;
import org.minimalj.example.erp.model.Customer;
import org.minimalj.example.erp.model.Offer;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.SearchPage;

public class ErpApplication extends Application {

	public ErpApplication() {
	}

	@Override
	public List<Action> getNavigation() {
		List<Action> actions = new ArrayList<>();
		
		ActionGroup customerActions = new ActionGroup("Kunden");
		actions.add(customerActions);
		customerActions.add(new CustomerTablePage());
		customerActions.add(new AddCustomerEditor());
		
		ActionGroup articleActions = new ActionGroup("Artikel");
		actions.add(articleActions);
		articleActions.add(new ArticleTablePage());
		articleActions.add(new AddArticleEditor());
		
		return actions;
	}	
	
	@Override
	public Page createSearchPage(String query) {
		ArticleSearchPage articleSearchPage = new ArticleSearchPage(query);
		CustomerSearchPage customerSearchPage = new CustomerSearchPage(query);
		return SearchPage.handle(articleSearchPage, customerSearchPage);
	}

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[]{Customer.class, Article.class, Offer.class};
	}
}

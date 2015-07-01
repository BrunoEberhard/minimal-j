package org.minimalj.example.erp;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.example.erp.frontend.editor.AddArticleEditor;
import org.minimalj.example.erp.frontend.editor.AddCustomerEditor;
import org.minimalj.example.erp.frontend.page.ArticleSearchPage;
import org.minimalj.example.erp.frontend.page.CustomerSearchPage;
import org.minimalj.example.erp.frontend.page.CustomerTablePage;
import org.minimalj.example.erp.model.Article;
import org.minimalj.example.erp.model.Customer;
import org.minimalj.example.erp.model.Offer;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.SearchPage;
import org.minimalj.frontend.toolkit.Action;
import org.minimalj.model.test.ModelTest;

public class ErpApplication extends Application {

	public ErpApplication() {
	}

	@Override
	public List<Action> getMenu() {
		List<Action> actions = new ArrayList<>();
		
		ActionGroup customerActions = new ActionGroup("Kunden");
		actions.add(customerActions);
		customerActions.add(new AddCustomerEditor());
		customerActions.add(new CustomerTablePage());
		
		ActionGroup articleActions = new ActionGroup("Artikel");
		actions.add(articleActions);
		articleActions.add(new AddArticleEditor());
		
		return actions;
	}	
	
	@Override
	public SearchPage[] getSearchPages() {
		return new SearchPage[] { new ArticleSearchPage(), new CustomerSearchPage() };
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

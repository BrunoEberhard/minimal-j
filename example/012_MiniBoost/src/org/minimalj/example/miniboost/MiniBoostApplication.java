package org.minimalj.example.miniboost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.minimalj.application.Application;
import org.minimalj.example.miniboost.frontend.AddCustomerEditor;
import org.minimalj.example.miniboost.frontend.CustomerSearchPage;
import org.minimalj.example.miniboost.model.Customer;
import org.minimalj.example.miniboost.model.Employee;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.SearchPage;
import org.minimalj.util.resources.Resources;

public class MiniBoostApplication extends Application {

	@Override
	protected Set<String> getResourceBundleNames() {
		return Collections.singleton(MiniBoostApplication.class.getName());
	}
	
	@Override
	public List<Action> getNavigation() {
		List<Action> menu = new ArrayList<>();

		ActionGroup groupOwner = new ActionGroup(Resources.getResourceName(Customer.class));
		groupOwner.add(new AddCustomerEditor());
		menu.add(groupOwner);
		
		return menu;
	}
	
//	@Override
//	public Page createDefaultPage() {
//		// return new HtmlPage("intro_minimalclinic.html", "Minimal Clinic");
//	}
	
	@Override
	public Page createSearchPage(String query) {
		return SearchPage.handle(new CustomerSearchPage(query));
	}
	
	@Override
	public Class<?>[] getEntityClasses() {
		return new Class[]{Customer.class, Employee.class};
	}
}

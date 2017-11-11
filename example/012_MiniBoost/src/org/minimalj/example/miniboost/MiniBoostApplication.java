package org.minimalj.example.miniboost;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.example.miniboost.frontend.AddCustomerEditor;
import org.minimalj.example.miniboost.frontend.AddProjectEditor;
import org.minimalj.example.miniboost.frontend.CustomerSearchPage;
import org.minimalj.example.miniboost.frontend.CustomerTablePage;
import org.minimalj.example.miniboost.frontend.ProjectTablePage;
import org.minimalj.example.miniboost.frontend.UserEditor;
import org.minimalj.example.miniboost.model.Customer;
import org.minimalj.example.miniboost.model.Employee;
import org.minimalj.example.miniboost.model.Project;
import org.minimalj.example.miniboost.model.ProjectCost;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.impl.nanoserver.NanoWebServer;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.SearchPage;
import org.minimalj.util.resources.Resources;

public class MiniBoostApplication extends Application {
	
	@Override
	public List<Action> getNavigation() {
		List<Action> menu = new ArrayList<>();

		ActionGroup groupOwner = new ActionGroup(Resources.getString(Customer.class));
		groupOwner.add(new CustomerTablePage());
		groupOwner.add(new AddCustomerEditor());
		menu.add(groupOwner);

		ActionGroup groupProject = new ActionGroup(Resources.getString(Project.class));
		groupProject.add(new ProjectTablePage());
		groupProject.add(new AddProjectEditor());
		menu.add(groupProject);

		ActionGroup groupAdmin = new ActionGroup("Setting");
		groupAdmin.add(new UserEditor());
		menu.add(groupAdmin);
		
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
		return new Class[]{Customer.class, Employee.class, Project.class, ProjectCost.class};
	}
	
	public static void main(String[] args) {
		Configuration.set("MjFrontendPort", "8081");
		NanoWebServer.start(new MiniBoostApplication());
	}
}

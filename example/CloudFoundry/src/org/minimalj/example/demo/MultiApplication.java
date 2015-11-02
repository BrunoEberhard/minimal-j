package org.minimalj.example.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.Page;

public class MultiApplication extends Application {

	private final Map<String, Application> applications = new HashMap<>();
	
	public void addApplication(String context, Application application) {
		applications.put(context, application);
	}

	@Override
	public List<Action> getNavigation() {
		return getCurrentApplication().getNavigation();
	}
	
	@Override
	public Class<?>[] getEntityClasses() {
		return getCurrentApplication().getEntityClasses();
	}

	@Override
	public String getName() {
		return getCurrentApplication().getName();
	}
	
	@Override
	public Page createDefaultPage() {
		return getCurrentApplication().createDefaultPage();
	}
	
	@Override
	public Page createSearchPage(String query) {
		return getCurrentApplication().createSearchPage(query);
	};
	
	private Application getCurrentApplication() {
		return applications.get(DemoContext.getContext());
	}
	
}

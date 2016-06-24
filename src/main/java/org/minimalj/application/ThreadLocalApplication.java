package org.minimalj.application;

import java.util.List;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.Page;

public class ThreadLocalApplication extends Application {

	private InheritableThreadLocal<Application> current = new InheritableThreadLocal<>();
	
	public ThreadLocalApplication() {
	}

	public void setCurrentApplication(Application application) {
		current.set(application);
	}
	
	private Application getCurrentApplication() {
		return current.get();
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
	public Page createSearchPage(String query) {
		return getCurrentApplication().createSearchPage(query);
	}
	
	@Override
	public Page createDefaultPage() {
		return getCurrentApplication().createDefaultPage();
	}
	
	@Override
	public List<Action> getNavigation() {
		return getCurrentApplication().getNavigation();
	}
}

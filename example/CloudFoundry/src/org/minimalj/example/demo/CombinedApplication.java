package org.minimalj.example.demo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.minimalj.application.Application;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.Page;

public class CombinedApplication extends Application {

	private final Map<String, Application> applications;
	private InheritableThreadLocal<Application> current = new InheritableThreadLocal<>();
	
	public CombinedApplication(Map<String, Application> applications) {
		Objects.nonNull(applications);
		if (applications.isEmpty()) throw new IllegalArgumentException();
		
		this.applications = applications;
	}

	public void setCurrentApplication(String name) {
		if (!applications.containsKey(name)) {
			throw new IllegalArgumentException(name);
		}
		current.set(applications.get(name));
	}
	
	private Application getCurrentApplication() {
		return current.get();
	}
	
	@Override
	public Class<?>[] getEntityClasses() {
		Set<Class<?>> classes = new HashSet<>();
		applications.values().forEach((application) -> classes.addAll(Arrays.asList(application.getEntityClasses())));
		return classes.toArray(new Class[classes.size()]);
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

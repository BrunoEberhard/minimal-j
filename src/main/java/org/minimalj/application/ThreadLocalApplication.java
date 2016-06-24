package org.minimalj.application;

import java.util.List;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.Page;

/**
 * <p>This class is only for cases where you really need several applications in one JVM.
 * Normally there is exactly one Application configured at startup and not further
 * change is made.</p>
 * 
 * <p>For tests or demo deployments it may be needed that the application can be replaced.
 * This is forbidden by the Application class itself. This extends can switch the current
 * Application.</p>
 *
 */
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

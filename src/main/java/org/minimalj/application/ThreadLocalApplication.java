package org.minimalj.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.Page;
import org.minimalj.repository.Repository;

/**
 * <p>Warning: This class is only for rare cases where you really need several applications in one JVM.
 * Normally there is exactly one Application configured at startup and no further
 * change is made. This class is not for production!</p>
 * 
 * <p>For tests or demo deployments it may be needed that the application can be replaced.
 * This is forbidden by the Application class itself. This extension can switch the current
 * Application and the repository.</p>
 * 
 * <p>Also note that this trick only works if Frontend and Backend are deployed in
 * the same JVM!</p>
 *
 */
public class ThreadLocalApplication extends Application {

	private final InheritableThreadLocal<Application> current = new InheritableThreadLocal<>();
	
	public ThreadLocalApplication() {
		Backend.setInstance(new ThreadLocalBackend());
	}

	public void setCurrentApplication(Application application) {
		current.set(application);
	}
	
	public Application getCurrentApplication() {
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

	public class ThreadLocalBackend extends Backend {
		private final Map<Application, Repository> repositories = new HashMap<>(); 

		@Override
		public Repository getRepository() {
			Application application = getCurrentApplication();
			if (!repositories.containsKey(application)) {
				super.setRepository(null); // this forces super.getRepository to create a new instance
				repositories.put(application, super.getRepository());
			}
			return repositories.get(application);
		}
		
		@Override
		public void setRepository(Repository repository) {
			repositories.put(getCurrentApplication(), repository);
		}
		
	}
}

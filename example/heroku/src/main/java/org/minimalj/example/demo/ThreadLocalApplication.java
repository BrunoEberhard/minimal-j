package org.minimalj.example.demo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.web.MjHttpExchange;
import org.minimalj.frontend.impl.web.MjHttpHandler;
import org.minimalj.frontend.impl.web.ResourcesHttpHandler;
import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.frontend.page.Page;
import org.minimalj.repository.Repository;
import org.minimalj.util.resources.MultiResourceBundle;
import org.minimalj.util.resources.Resources;

/**
 * This is just a hack to make all the examples work in one application on heroku.
 *
 */
public class ThreadLocalApplication extends WebApplication {

	public static final ThreadLocalApplication INSTANCE = new ThreadLocalApplication();

	private final InheritableThreadLocal<Application> current = new InheritableThreadLocal<>();
	private boolean backendSet = false;
	
	private Collection<Application> applications;
	
	public ThreadLocalApplication() {
		// private
	}
	
	public void setApplications(Collection<Application> applications) {
		this.applications = applications;
	}
	
	public void setCurrentApplication(Application application) {
		if (!backendSet) {
			Backend.setInstance(new ThreadLocalBackend());
			backendSet = true;
		}
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
	public void search(String query) {
		getCurrentApplication().search(query);
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
	
	@Override
	protected MjHttpHandler createHttpHandler() {
		return new ThreadLocalResourcesHttpHandler();
	}
	
	private class ThreadLocalResourcesHttpHandler implements MjHttpHandler {

		@Override
		public void handle(MjHttpExchange exchange) {
			String path = exchange.getPath();
			handle(exchange, path);
		}

		public void handle(MjHttpExchange exchange, String path) {
			int pos = path.lastIndexOf('.');
			if (pos > 0 && pos < path.length() - 1) {
				String suffix = path.substring(pos + 1);
				if (path.contains("..")) {
					exchange.sendForbidden();
					return;
				}

				String mimeType = Resources.getMimeType(suffix);
				if (mimeType != null) {
					byte[] bytes = getResource(path);
					if (bytes != null) {
						exchange.sendResponse(200, bytes, mimeType);
					}
				}
			}
		}

		public URL getUrl(String path) throws IOException {
			return getCurrentApplication().getClass().getResource("web/" + path);
		}

		private InputStream getInputStream(String path) throws IOException {
			URL url = getUrl(path);
			return url != null ? url.openStream() : null;
		}

		private byte[] getResource(String path) {
			try (InputStream inputStream = getInputStream(path)) {
				if (inputStream != null) {
					return ResourcesHttpHandler.read(inputStream);
				}
				return null;
			} catch (IOException x) {
				throw new RuntimeException(x);
			}
		}
	}
	
	@Override
	public ResourceBundle getResourceBundle(Locale locale) {
		List<ResourceBundle> resourceBundles = new ArrayList<>();
		for (Application a : applications) {
			ResourceBundle r = a.getResourceBundle(locale);
			if (r != null) {
				resourceBundles.add(r);
			}
		}
		return new MultiResourceBundle(resourceBundles);
	}
}

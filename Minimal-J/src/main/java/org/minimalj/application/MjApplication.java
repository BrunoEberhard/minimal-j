package org.minimalj.application;

import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.minimalj.backend.Backend;
import org.minimalj.backend.db.DbPersistence;
import org.minimalj.frontend.page.EmptyPage;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageContext;
import org.minimalj.frontend.toolkit.IAction;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

/**
 * Extend this class as main entry for your Application.<p>
 * 
 * Set the argument <code>MjApplication</code> as VM argument if the application
 * is started with SwingLauncher or set the <code>init-param</code> in the servlet
 * element in the <code>web.xml</code> if the VaadinLauncher is used.
 *
 */
public abstract class MjApplication {
	private static final Logger logger = Logger.getLogger(DbPersistence.class.getName());
	private static MjApplication application;
	
	public static MjApplication getApplication() {
		if (application == null) {
			throw new IllegalStateException("CientApplicationConfig has to be initialized");
		}
		return application;
	}
	
	private static synchronized void setApplication(MjApplication application) {
		if (MjApplication.application != null) {
			throw new IllegalStateException("Application cannot be changed");
		}		
		if (application == null) {
			throw new IllegalArgumentException("Application cannot be null");
		}
		MjApplication.application = application;
	}
	
	/**
	 * 
	 * @param simplePackageName for example "editor".
	 * @return the package name of this type of package for this application. For example "ch.openech.mj.example.editor"
	 */
	public static String getCompletePackageName(String simplePackageName) {
		MjApplication application = MjApplication.getApplication();
		String applicationClassName = application.getClass().getName();
		int pos = applicationClassName.lastIndexOf(".");
		if (pos  >= 0) {
			return applicationClassName.substring(0, pos + 1) + simplePackageName;
		} else {
			return applicationClassName + "." + simplePackageName;
		}
	}
	
	protected MjApplication() {
		setApplication(this);
		initBackend();
		ResourceBundle resourceBundle = getResourceBundle();
		if (resourceBundle != null) Resources.addResourceBundle(resourceBundle);
	}
	
	private void initBackend() {
		String backendAddress = System.getProperty("MjBackendAddress");
		String backendPort = System.getProperty("MjBackendPort", "8020");
		if (backendAddress != null) {
			Backend.setSocketBackend(backendAddress, Integer.valueOf(backendPort));
			return;
		} 

		String database = System.getProperty("MjBackendDatabase");
		String user= System.getProperty("MjBackendDataBaseUser", "APP");
		String password = System.getProperty("MjBackendDataBasePassword", "APP");
		if (!StringUtils.isBlank(database)) {
			Backend.setDbBackend(database, user, password);
			return;
		}
		
		String backendClassName = System.getProperty("MjBackend");
		if (!StringUtils.isBlank(backendClassName)) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends Backend> backendClass = (Class<? extends Backend>) Class.forName(backendClassName);
				Backend backend = backendClass.newInstance();
				Backend.setInstance(backend);
			} catch (Exception x) {
				throw new LoggingRuntimeException(x, logger, "Set backend failed");
			}
			return;
		} 
		
		Backend.setEmbeddedDbBackend();
	}
	
	/**
	 * note: Use MultiResourceBundle if more than one ResourceBundle
	 * should be loaded.
	 * @return The application specific ResourceBundle.
	 */
	protected ResourceBundle getResourceBundle() {
		try {
			return ResourceBundle.getBundle(this.getClass().getName());
		} catch (MissingResourceException x) {
			Logger logger = Logger.getLogger(MjApplication.class.getName());
			logger.warning("Missing the default ResourceBundle for " + this.getClass().getName());
			logger.fine("The default ResourceBundle has the same name as the Application that is launched.");
			logger.fine("See the MjExampleApplication.java and MjExampleApplication.properties");
			return null;
		}
	}

	public abstract Class<?>[] getEntityClasses();

	public abstract String getWindowTitle(PageContext pageContext);
	
	public abstract Class<?> getPreferencesClass();
	
	public abstract Class<?>[] getSearchClasses();
	
	public Page createDefaultPage(PageContext context) {
		return new EmptyPage(context);
	}
	
	public List<IAction> getActionsNew(PageContext context) {
		// should be done in subclass
		return Collections.emptyList();
	}

	public List<IAction> getActionsImport(PageContext context) {
		return Collections.emptyList();
	}

	public List<IAction> getActionsExport(PageContext context) {
		return Collections.emptyList();
	}

	public List<IAction> getActionsView(PageContext context) {
		return Collections.emptyList();
	}

	public void init() {
		// can be used in concrete implementation, but use with care
	}
	
}

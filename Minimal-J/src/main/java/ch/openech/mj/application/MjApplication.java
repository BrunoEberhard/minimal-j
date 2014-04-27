package ch.openech.mj.application;

import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import ch.openech.mj.page.EmptyPage;
import ch.openech.mj.page.Page;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.server.Services;
import ch.openech.mj.toolkit.IAction;

/**
 * Extend this class as main entry for your Application.<p>
 * 
 * Set the argument <code>MjApplication</code> as VM argument if the application
 * is started with SwingLauncher or set the <code>init-param</code> in the servlet
 * element in the <code>web.xml</code> if the VaadinLauncher is used.
 *
 */
public abstract class MjApplication {
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
		initServices();
		ResourceBundle resourceBundle = getResourceBundle();
		if (resourceBundle != null) Resources.addResourceBundle(resourceBundle);
	}
	
	private void initServices() {
		String serviceAddress = System.getProperty("MjServicesAddress");
		String serivcePort = System.getProperty("MjServicesPort", "8020");
		if (serviceAddress != null) {
			Services.configureRemoteSocket(serviceAddress, Integer.valueOf(serivcePort));
		} else {
			Services.configureLocal();
		}
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

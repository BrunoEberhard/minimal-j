package org.minimalj.application;

import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.minimalj.frontend.page.EmptyPage;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageContext;
import org.minimalj.frontend.toolkit.IAction;
import org.minimalj.util.resources.Resources;

/**
 * Extend this class as main entry for your Application.<p>
 * 
 * Set the argument <code>MjApplication</code> as argument if the application
 * is started with SwingLauncher or set the <code>init-param</code> in the servlet
 * element in the <code>web.xml</code> if the VaadinLauncher is used.<p>
 *
 * The only method you have to override is getEntityClasses to specify the
 * main classes of your business data model. All other methods are optional.
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
	
	public static synchronized void setApplication(MjApplication application) {
		if (MjApplication.application != null) {
			throw new IllegalStateException("Application cannot be changed");
		}		
		if (application == null) {
			throw new IllegalArgumentException("Application cannot be null");
		}
		MjApplication.application = application;
		
		ResourceBundle resourceBundle = application.getResourceBundle();
		if (resourceBundle != null) Resources.addResourceBundle(resourceBundle);
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

	public String getName() {
		if (Resources.getResourceBundle().containsKey("Application.title")) {
			return Resources.getResourceBundle().getString("Application.title");
		} else {
			return getClass().getSimpleName();
		}
	}
	
	public Class<?> getPreferencesClass() {
		return null;
	}
	
	public Class<?>[] getSearchClasses() {
		return new Class<?>[0];
	}
	
	public Page createDefaultPage(PageContext context) {
		return new EmptyPage(context);
	}
	
	public List<IAction> getActionsNew(PageContext context) {
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
	
}

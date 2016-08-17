/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.minimalj.application;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.nanoserver.NanoWebServer;
import org.minimalj.frontend.impl.swing.Swing;
import org.minimalj.frontend.page.EmptyPage;
import org.minimalj.frontend.page.Page;
import org.minimalj.security.Subject;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

/**
 * Extend this class to configure your Application.
 * Both frontend and backend refer to this class.<p>
 * 
 * Set the first argument of the JVM to your extension of this class if the application
 * is started with Swing or NanoWebServer as main class or set the <code>init-param</code> in the servlet
 * element in the <code>web.xml</code> if a web server is used.<p>
 *
 * All non static methods can be overridden to define the behavior
 * of the application.
 * 
 * @see Swing
 * @see NanoWebServer
 */
public abstract class Application {
	private static Application instance;
	
	public Application() {
		for (String resourceBundleName : getResourceBundleNames()) {
			Resources.addResourceBundleName(resourceBundleName);
		}
	}
	
	public static Application getInstance() {
		if (instance == null) {
			throw new IllegalStateException("Application has to be initialized");
		}
		return instance;
	}
	
	/**
	 * Sets the application of this vm. Can only be called once.
	 * This method should only be called by a frontend or a backend main class.
	 * 
	 * @param application normally the created by createApplication method
	 */
	public static void setInstance(Application application) {
		if (Application.instance != null) {
			throw new IllegalStateException("Application cannot be changed");
		}		
		if (application == null) {
			throw new IllegalArgumentException("Application cannot be null");
		}
		Application.instance = application;
	}
	
	/**
	 * In tests it may be needed to have more than one instance of an application.
	 * Warning: Use with care. Works only if Frontend and Backend are in the same JVM!
	 * 
	 * @param application the application for current thread and all its children
	 */
	public static void setThreadInstance(Application application) {
		if (instance == null) {
			instance = new ThreadLocalApplication();
		} else if (!(instance instanceof ThreadLocalApplication)) {
			throw new IllegalStateException();
		}
		((ThreadLocalApplication) instance).setCurrentApplication(application);
	}
	
	/**
	 * This is just a shortcut for creating the application from jvm arguments.
	 * Most frontend or backend main classes use this method
	 * 
	 * @param args the arguments provided to the jvm
	 */
	public static void initApplication(String[] args) {
		if (args.length < 1) {
			throw new IllegalArgumentException("Please specify an Application as first argument");
		}
		
		String applicationClassName = args[0];
		Application application = createApplication(applicationClassName);
		Application.setInstance(application);
	}
	
	/**
	 * Creates the Application from a class name. This method should normally only
	 * be called by a frontend or a backend main class.
	 * 
	 * @param applicationClassName qualified class name
	 * @return the created application. Different exceptions are thrown if the
	 * creation failed.
	 */
	public static Application createApplication(String applicationClassName) {
		Class<?> applicationClass;
		try {
			applicationClass = Class.forName(applicationClassName);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Could not found Application class: " + applicationClassName);
		}
		Object application;
		try {
			application = applicationClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException("Could not instantiate Application class: " + applicationClassName, e);
		}
		
		if (!(application instanceof Application)) {
			throw new IllegalArgumentException("Class " + applicationClassName + " doesn't extend Application");
		}
		
		return (Application) application;
	}

	/**
	 * @return The application specific ResourceBundle names
	 */
	protected Set<String> getResourceBundleNames() {
		try {
			// try to load the bundle to provoke the exception if resource bundle is missing
			ResourceBundle.getBundle(this.getClass().getName());
			return Collections.singleton(this.getClass().getName());
		} catch (MissingResourceException x) {
			Logger logger = Logger.getLogger(Application.class.getName());
			logger.warning("Missing the default ResourceBundle for " + this.getClass().getName());
			logger.fine("The default ResourceBundle has the same name as the Application that is launched.");
			logger.fine("See the MjExampleApplication.java and MjExampleApplication.properties");
			return Collections.emptySet();
		}
	}

	/**
	 * Defines the (root) entities of this application. These are the classes the are used
	 * for persistence. Classes only used as base for an editor should not be listed here.
	 * 
	 * @return all the classes used for persistence. These classes will be checked for compliance by the ModelTest .
	 */
	public Class<?>[] getEntityClasses() {
		return new Class<?>[0];
	}

	public String getName() {
		if (Resources.isAvailable(Resources.APPLICATION_NAME)) {
			return Resources.getString(Resources.APPLICATION_NAME);
		} else {
			return getClass().getSimpleName();
		}
	}
	
	public InputStream getIcon() {
		String applicationIconName;
		if (Resources.isAvailable(Resources.APPLICATION_ICON)) {
			applicationIconName = Resources.getString(Resources.APPLICATION_ICON);
		} else {
			applicationIconName = getClass().getSimpleName() + ".png";
		}
		InputStream icon = getClass().getResourceAsStream(applicationIconName);
		if (icon == null) {
			icon = getClass().getResourceAsStream("/" + applicationIconName);
		}
		return icon;
	}
	
	/**
	 * If more than one class of entities should be search have a look at
	 * SearchPage.handle(SearchPage...)
	 * 
	 * @param query the string the user entered in the search field
	 * @return the page to be displayed for the query string
	 */
	public Page createSearchPage(String query) {
		return new EmptyPage();
	}
	
	/**
	 * note: for production you should override this method to avoid continuous use of reflection
	 * 
	 * @return true if the application overrides createSearchPage meaning
	 * the application provides a search page
	 */
	public boolean hasSearchPages() {
		try {
			return this.getClass().getMethod("createSearchPage", new Class<?>[] { String.class }).getDeclaringClass() != Application.class;
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	public Page createDefaultPage() {
		return new EmptyPage();
	}
	
	/**
	 * 
	 * @return true if this application cannot be accessed without login
	 */
	public boolean isLoginRequired() {
		return false;
	}
	
	/**
	 * If the list of actions depend on the currently logged in user you can
	 * check if the user has the needed roles by {@link Subject#hasRole(String...)}
	 * 
	 * @return this list of action that the current subject is allowed to execute.
	 */
	public List<Action> getNavigation() {
		return Collections.emptyList();
	}
	
	static {
		// Feel free to set your own log format. For me the two line default format of java is terrible.
		// note: the default handler streams to System.err . This is why the output in eclipse is red.
		// I haven't figured out yet how this can be changed easily. 
		if (StringUtils.isEmpty(System.getProperty("java.util.logging.SimpleFormatter.format"))) {
			System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT:%1$tL %4$-13s %3$-4s %5$s %6$s%n");
		}
	}
	
	public static void main(String[] args) {
		Logger logger = Logger.getLogger(Application.class.getName());
		logger.warning("Starting the Application class is not the intended way to start a Minimal-J application");
		String mainClass = System.getProperty("sun.java.command");
		if (mainClass == null) {
			logger.severe("and the started application could not be retrieved. Nothing started.");
		} else if (Application.class.getName().equals(mainClass)) {
			logger.severe("and starting the Application class doesn't work at all. Nothing started.");
		} else {
			Swing.main(new String[]{mainClass});
		}
	}
	
}

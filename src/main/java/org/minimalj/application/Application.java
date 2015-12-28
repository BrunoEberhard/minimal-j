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

import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;

import org.minimalj.backend.SocketBackendServer;
import org.minimalj.backend.sql.SqlBackend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.swing.SwingApplication;
import org.minimalj.frontend.page.EmptyPage;
import org.minimalj.frontend.page.Page;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

/**
 * Extend this class as the start point for your Application.
 * Both frontend and backend get their application
 * specification from this class.<p>
 * 
 * Set the first argument of the JVM if the application
 * is started with SwingFrontend as main class or set the <code>init-param</code> in the servlet
 * element in the <code>web.xml</code> if a web server is used.<p>
 *
 * All non static methods can be overridden to define the behavior
 * of the application.
 * 
 * @see SwingApplication
 * @see SocketBackendServer
 * @see SqlBackend
 */
public abstract class Application {
	private static Application application;
	
	public static Application getApplication() {
		if (application == null) {
			throw new IllegalStateException("Application has to be initialized");
		}
		return application;
	}
	
	public Application() {
		for (String resourceBundleName : getResourceBundleNames()) {
			Resources.addResourceBundleName(resourceBundleName);
		}
	}
	
	/**
	 * Sets the application of this vm. Can only be called once.
	 * This method should only be called by a frontend or a backend main class.
	 * 
	 * @param application normally the created by createApplication method
	 */
	public static synchronized void setApplication(Application application) {
		if (Application.application != null) {
			throw new IllegalStateException("Application cannot be changed");
		}		
		if (application == null) {
			throw new IllegalArgumentException("Application cannot be null");
		}
		Application.application = application;
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
		Application.setApplication(application);
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

	public Class<?>[] getEntityClasses() {
		return new Class<?>[0];
	}

	public String getName() {
		if (Resources.getResourceBundle().containsKey("Application.title")) {
			return Resources.getResourceBundle().getString("Application.title");
		} else {
			return getClass().getSimpleName();
		}
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

	public Page createDefaultPage() {
		return new EmptyPage();
	}
	
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
	
	public static void main(String[] args) throws Exception {
		Logger logger = Logger.getLogger(Application.class.getName());
		logger.warning("Starting the Application class is not the intended way to start a Minimal-J application");
		String mainClass = System.getProperty("sun.java.command");
		if (mainClass == null) {
			logger.severe("and the started application could not be retrieved. Nothing started.");
		} else if (Application.class.getName().equals(mainClass)) {
			logger.severe("and starting the Application class doesn't work at all. Nothing started.");
		} else {
			SwingApplication.main(new String[]{mainClass});
		}
	}
	
}

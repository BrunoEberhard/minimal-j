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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.logging.Logger;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.swing.Swing;
import org.minimalj.frontend.impl.web.WebServer;
import org.minimalj.frontend.page.EmptyPage;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.Routing;
import org.minimalj.model.Model;
import org.minimalj.repository.Repository;
import org.minimalj.repository.sql.SqlRepository;
import org.minimalj.security.Authentication;
import org.minimalj.security.Subject;
import org.minimalj.security.TextFileAuthentication;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.MultiResourceBundle;
import org.minimalj.util.resources.Resources;

/**
 * Extend this class to define your Application.<p>
 * 
 * All non static methods can be overridden for configuration.<p>
 * 
 * The specific application can be started with the provided main method.<p>
 * 
 * @see WebServer NanoWebServer - Start the class as stand alone web application
 * @see Swing Swing - Start the application as desktop application
 */
public abstract class Application implements Model {
	private static Application instance;
	
	public Application() {
		//
	}
	
	public static Application getInstance() {
		if (instance == null) {
			throw new IllegalStateException("Application has to be initialized");
		}
		return instance;
	}
	
	/**
	 * Sets the application of this vm. Can only be called once (except a second
	 * time with the same application). This method should only be called by a
	 * frontend or a backend main class.
	 * 
	 * @param application normally the created by createApplication method
	 */
	public static void setInstance(Application application) {
		if (application == Application.instance) {
			return;
		}
		if (Application.instance != null) {
			throw new IllegalStateException("Application cannot be changed");
		}		
		Application.instance = application;
	}
		
	/**
	 * This is just a shortcut for creating the application from jvm arguments.
	 * Most frontend main classes use this method
	 * 
	 * @param args the arguments provided to the jvm
	 */
	public static void initApplication(String... args) {
		if (args.length < 1) {
			System.err.println("Please specify an Application as argument");
			System.exit(-1);
		}
		setInstance(createApplicationByClassName(args[0]));
	}
	
	private static Application createApplicationByClassName(String applicationClassName) {
		Class<?> applicationClass;
		try {
			applicationClass = Class.forName(applicationClassName);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Could not find Application class: " + applicationClassName);
		}
		Object application;
		try {
			application = applicationClass.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("Could not instantiate Application class: " + applicationClassName, e);
		}
		
		if (!(application instanceof Application)) {
			throw new IllegalArgumentException("Class " + applicationClassName + " doesn't extend Application");
		}

		return (Application) application;
	}
	
	/**
	 * 
	 * @param locale
	 *            the Locale
	 * @return The ResourceBundle for the application and the locale. The default
	 *         name of the properties file is the same as the application class
	 *         name.
	 * @see MultiResourceBundle MultiResourceBundle to combine several
	 *      ResourceBundle
	 */
	public ResourceBundle getResourceBundle(Locale locale) {
		return new MultiResourceBundle(loadResourceBundles(locale, getClass()));
	}
	
	public static List<ResourceBundle> loadResourceBundles(Locale locale, Class<?> clazz) {
		List<ResourceBundle> resourceBundles = new ArrayList<>();
		do {
			try {
				String resourceBundleName = clazz.getSimpleName();
				resourceBundles.add(ResourceBundle.getBundle(resourceBundleName, locale, Control.getNoFallbackControl(Control.FORMAT_PROPERTIES)));
			} catch (MissingResourceException x) {
				if (clazz == getInstance().getClass()) {
					Logger logger = Logger.getLogger(Application.class.getName());
					logger.warning("Missing the default ResourceBundle for " + clazz.getName());
					logger.fine("The default ResourceBundle has the same name as the Application that is launched.");
					logger.fine("See the MjExampleApplication.java and MjExampleApplication.properties");
				}
			}
			clazz = clazz.getSuperclass();
		} while (clazz != null && clazz != Application.class);
		return resourceBundles;
	}

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[0];
	}
	
	public Repository createRepository() {
		return new SqlRepository(this);
	}

	public Authentication createAuthentication() {
		String userFile = Configuration.get("MjUserFile");
		if (userFile != null) {
			return new TextFileAuthentication(userFile);
		} else {
			return null;
		}
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
		if (icon == null) {
			icon = getClass().getResourceAsStream("/application_16.png");
		}		
		return icon;
	}
	
	public Routing createRouting() {
		return null;
	}
	
	/**
	 * If more than one class of entities should be searched have a look at
	 * SearchPage.handle(SearchPage...)
	 * 
	 * @param query the string the user entered in the search field
	 */
	public void search(String query) {
		Frontend.show(new EmptyPage());
	}
	
	/**
	 * note: for production you should override this method to avoid continuous
	 * use of reflection
	 * 
	 * @return true if the application overrides createSearchPage meaning the
	 *         application provides a search page. Can also depend on the current Subject.
	 */
	public boolean hasSearch() {
		try {
			return this.getClass().getMethod("search", String.class).getDeclaringClass() != Application.class;
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	public enum AuthenticatonMode {
		REQUIRED, SUGGESTED, OPTIONAL, NOT_AVAILABLE;
		
		public boolean showLoginAtStart() {
			return this == REQUIRED || this == SUGGESTED;
		}
	}
	
	public AuthenticatonMode getAuthenticatonMode() {
		if (Backend.getInstance().isAuthenticationActive()) {
			return AuthenticatonMode.REQUIRED;
		} else {
			return AuthenticatonMode.NOT_AVAILABLE;
		}
	}
	
	/**
	 * called for a new User or when the user did login or logout
	 *
	 * @return Page to be displayed in (possible new) page container
	 */
	public Page createDefaultPage() {
		return new EmptyPage();
	}

	/** 
	 * called when a Backend is initialized (once per VM)
	 * 
	 */
	public void initBackend() {
		// application specific	
	}
	
	/**
	 * called when the backend completed a transaction
	 * 
	 */
	public void transactionCompleted(Transaction<?> transaction) {
		// application specific	
	}
	
	/** 
	 * called when a Frontend is initialized (once per VM)
	 * 
	 */
	public void initFrontend() {
		// application specific	
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
	
	public static void main(String... args) {
		Logger logger = Logger.getLogger(Application.class.getName());
		logger.warning("Starting the Application class is not the intended way to start a Minimal-J application");
		String mainClass = System.getProperty("sun.java.command");
		if (mainClass == null) {
			logger.severe("and the started application could not be retrieved. Nothing started.");
		} else if (Application.class.getName().equals(mainClass)) {
			logger.severe("and starting the Application class doesn't work at all. Nothing started.");
		} else {
			WebServer.main(mainClass);
		}
	}
	
}
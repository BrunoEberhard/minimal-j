package org.minimalj.application;


public abstract class Launcher {

	public static void initApplication(String[] args) {
		if (args.length < 1) {
			throw new IllegalArgumentException("Please specify a MjApplication as first argument");
		}
		
		String applicationClassName = args[0];
		MjApplication application = createApplication(applicationClassName);
		MjApplication.setApplication(application);
	}
	
	public static MjApplication createApplication(String applicationClassName) {
		Class<?> applicationClass;
		try {
			applicationClass = Class.forName(applicationClassName);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Could not found MjApplication class: " + applicationClassName);
		}
		Object application;
		try {
			application = applicationClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException("Could not instantiate MjApplication class: " + applicationClassName, e);
		}
		
		if (!(application instanceof MjApplication)) {
			throw new IllegalArgumentException("Class " + applicationClassName + " doesn't extend MjApplication");
		}
		
		return (MjApplication) application;
	}
	
}

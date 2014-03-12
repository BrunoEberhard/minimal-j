package ch.openech.mj.application;

public abstract class ApplicationContext {

	private static ThreadLocal<ApplicationContext> contextByThread = new ThreadLocal<>();
	private Object preferences;

	public static void setApplicationContext(ApplicationContext context) {
		contextByThread.set(context);
	}

	public static ApplicationContext getApplicationContext() {
		return contextByThread.get();
	}
	
	public abstract void setUser(String user);

	public abstract String getUser();

	public Object getPreferences() {
		if (preferences == null) {
			preferences = newPreferencesInstance();
			loadPreferences(preferences);
		}
		return preferences;
	}

	private Object newPreferencesInstance() {
		Class<?> preferencesClass = MjApplication.getApplication().getPreferencesClass();
		if (preferencesClass == null)
			return new Object();
		try {
			return preferencesClass.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public abstract void loadPreferences(Object preferences);

	public abstract void savePreferences(Object preferences);
}

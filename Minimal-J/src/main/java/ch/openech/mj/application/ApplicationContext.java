package ch.openech.mj.application;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

public abstract class ApplicationContext {

	private Object preferences;
	private List<SoftReference<PreferenceChangeListener>> listeners = new ArrayList<SoftReference<PreferenceChangeListener>>();

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

	public void addPreferenceChangeListener(PreferenceChangeListener preferenceChangeListener) {
		SoftReference<PreferenceChangeListener> softReference = new SoftReference<PreferenceChangeListener>(
				preferenceChangeListener);
		listeners.add(softReference);
	}

	public void preferenceChange() {
		for (int i = listeners.size() - 1; i >= 0; i--) {
			SoftReference<PreferenceChangeListener> softReference = listeners.get(i);
			PreferenceChangeListener preferenceChangeListener = softReference.get();
			if (preferenceChangeListener != null) {
				preferenceChangeListener.preferenceChange();
			} else {
				listeners.remove(i);
			}
		}
	}

	private static interface PreferenceChangeListener {
		void preferenceChange();
	}

}

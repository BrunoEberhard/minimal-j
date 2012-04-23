package ch.openech.mj.application;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

public class ApplicationContext {

	private Object preferences;
	private List<SoftReference<PreferenceChangeListener>> listeners = new ArrayList<SoftReference<PreferenceChangeListener>>();

	public Object getPreferences() {
		return preferences;
	}
	
	public void setPreferences(Object preferences) {
		this.preferences = preferences;
	}
	
	public void addPreferenceChangeListener(PreferenceChangeListener preferenceChangeListener) {
		SoftReference<PreferenceChangeListener> softReference = new SoftReference<PreferenceChangeListener>(preferenceChangeListener);
		listeners.add(softReference);
	}
	
	public void preferenceChange() {
    	for (int i = listeners.size()-1; i>= 0; i--) {
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

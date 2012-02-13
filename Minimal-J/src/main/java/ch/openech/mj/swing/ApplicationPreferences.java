package ch.openech.mj.swing;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;


public class ApplicationPreferences implements PreferenceChangeListener {

	private static final ApplicationPreferences instance = new ApplicationPreferences();
	private List<SoftReference<PreferenceChangeListener>> listeners = new ArrayList<SoftReference<PreferenceChangeListener>>();

	private ApplicationPreferences() {
		// private
	}
	
	private void add(PreferenceChangeListener preferenceChangeListener) {
    	if (listeners.isEmpty()) {
    		System.out.println("Add to AbstractApplication");
    		PreferencesHelper.preferences().addPreferenceChangeListener(instance);
    	}
		SoftReference<PreferenceChangeListener> softReference = new SoftReference<PreferenceChangeListener>(preferenceChangeListener);
		listeners.add(softReference);
	}
	
    @Override
	public void preferenceChange(PreferenceChangeEvent evt) {
    	System.gc();
    	for (int i = listeners.size()-1; i>= 0; i--) {
    		SoftReference<PreferenceChangeListener> softReference = listeners.get(i);
    		PreferenceChangeListener preferenceChangeListener = softReference.get();
        	if (preferenceChangeListener != null) {
        		preferenceChangeListener.preferenceChange(evt);
        	} else {
        		listeners.remove(i);
        		System.out.println("Remove listener");
        	}
    	}
    	if (listeners.isEmpty()) {
    		PreferencesHelper.preferences().removePreferenceChangeListener(instance);
    		System.out.println("Remove from AbstractApplication");
    	}
    }
    
	public static void addPreferenceChangeListener(PreferenceChangeListener preferenceChangeListenerList) {
		instance.add(preferenceChangeListenerList);
	}
}

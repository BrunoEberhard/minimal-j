package ch.openech.mj.swing;

import java.lang.ref.SoftReference;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

public abstract class SoftPreferenceChangeListener implements PreferenceChangeListener {

	private final SoftReference<PreferenceChangeListener> reference;
	
	public SoftPreferenceChangeListener(PreferenceChangeListener preferenceChangeListener) {
		reference = new SoftReference<PreferenceChangeListener>(preferenceChangeListener);
	}
	
    public void preferenceChange(PreferenceChangeEvent evt) {
    	PreferenceChangeListener preferenceChangeListener = reference.get();
    	if (preferenceChangeListener != null) {
    		preferenceChangeListener.preferenceChange(evt);
    	}
    }

}

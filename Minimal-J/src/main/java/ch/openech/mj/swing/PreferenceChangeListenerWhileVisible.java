package ch.openech.mj.swing;

import java.awt.Component;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

public class PreferenceChangeListenerWhileVisible implements PreferenceChangeListener, HierarchyListener {

	private final Preferences preferences;
	private final Component component;
	private PreferenceChangeListener preferenceChangeListener;
	private boolean registred = false;
	
	public PreferenceChangeListenerWhileVisible(Preferences preferences, Component component) {
		this.preferences = preferences;
		this.component = component;
		updateRegistred();
		component.addHierarchyListener(this);
	}

	private void updateRegistred() {
		if (!registred && component.isShowing()) {
			preferences.addPreferenceChangeListener(this);
			registred = true;
		} else if (registred && !component.isShowing()) {
			preferences.removePreferenceChangeListener(this);
			registred = false;
		}
	}
	
	@Override
	public void hierarchyChanged(HierarchyEvent e) {
		updateRegistred();
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent evt) {
		if (preferenceChangeListener != null) {
			preferenceChangeListener.preferenceChange(evt);
		}
	}

	public void setPreferenceChangeListener(PreferenceChangeListener preferenceChangeListener) {
		this.preferenceChangeListener = preferenceChangeListener;
	}
	
}

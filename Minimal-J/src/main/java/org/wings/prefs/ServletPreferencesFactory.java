package org.wings.prefs;

import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

public class ServletPreferencesFactory
    implements PreferencesFactory
{
    public Preferences systemRoot() {
        return ServletPreferences.getSystemRoot();
    }

    @Override
	public Preferences userRoot() {
        return ServletPreferences.getUserRoot();
    }
}

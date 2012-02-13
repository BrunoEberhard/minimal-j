package ch.openech.mj.example;

import java.util.prefs.Preferences;

import ch.openech.mj.application.ApplicationConfig;
import ch.openech.mj.swing.FrameManager;
import ch.openech.mj.swing.PreferencesHelper;
import ch.openech.mj.swing.SwingApplication;


public class MjExampleApplication implements Runnable {

	public void initializePreferences() {
//		presetPreference("dateFormat", false);
//		presetPreference("codesFree", false);
//		presetPreference("codesClear", false);
//		presetPreference("generateData", true);
//		presetPreference("showXml", true);
	}
	
	@Override
	public void run() {
		PreferencesHelper.setPreferences(Preferences.userNodeForPackage(MjExampleApplication.class));
		FrameManager.setSystemLookAndFeel();
		
		ExamplePersistence.getInstance();
		
		FrameManager.getInstance().openNavigationFrame(ApplicationConfig.getApplicationConfig().getInitialWindowConfig());
	}

	public static void main(final String[] args) {
		SwingApplication.launch(new MjExampleApplication(), new ApplicationConfigExample());
	}

	
}

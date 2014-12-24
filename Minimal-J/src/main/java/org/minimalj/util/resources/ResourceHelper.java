package org.minimalj.util.resources;

import java.net.URL;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.minimalj.util.StringUtils;

public class ResourceHelper {
	private static final Logger logger = Logger.getLogger(ResourceHelper.class.getName());
	private static final String ICONS_DIRECTORY = "icons";
	private static Set<String> loggedMissings = new HashSet<>();
	
	public static Action initProperties(Action action, ResourceBundle resourceBundle, String baseName) {
		String text = getString(resourceBundle, baseName);
		if (text != null) {
			int mnemonicIndex = getMnemonicIndex(text);
			if (text.indexOf('\'') > -1) {
				text = text.replace("'&'", "&");
			}
			action.putValue(Action.NAME, text);
			if (mnemonicIndex > 0) {
				action.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, mnemonicIndex);
				action.putValue(Action.MNEMONIC_KEY, text.charAt(mnemonicIndex));
			}
		}
		
		Integer mnemonic = getKeyCode(resourceBundle, baseName + ".mnemonic");
		if (mnemonic != null) {
			action.putValue(Action.MNEMONIC_KEY, mnemonic);
		}
		
		Integer index = getInteger(resourceBundle, baseName + ".displayedMnemonicIndex");
		if (index != null) {
			action.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, index);
		}
		
		KeyStroke key = getKeyStroke(resourceBundle, baseName + ".accelerator");
		if (key != null) {
			action.putValue(Action.ACCELERATOR_KEY, key);
		}
		
		Icon icon = getIcon(resourceBundle, baseName + ".icon");
		if (icon != null) {
			action.putValue(Action.SMALL_ICON, icon);
			action.putValue(Action.LARGE_ICON_KEY, icon);
		}
		
		Icon smallIcon = getIcon(resourceBundle, baseName + ".smallIcon");
		if (smallIcon != null) {
			action.putValue(Action.SMALL_ICON, smallIcon);
		}
		
		Icon largeIcon = getIcon(resourceBundle, baseName + ".largeIcon");
		if (largeIcon != null) {
			action.putValue(Action.LARGE_ICON_KEY, largeIcon);
		}
		
		addValue(action, Action.SHORT_DESCRIPTION, resourceBundle, baseName + ".shortDescription");
		addValue(action, Action.LONG_DESCRIPTION, resourceBundle, baseName + ".longDescription");
		
		return action;
	}
	
	private static void addValue(Action action, String actionKey, ResourceBundle resourceBundle, String property) {
		if (resourceBundle.containsKey(property)) {
			action.putValue(actionKey, resourceBundle.getString(property));
		}
	}

	public static Icon getIcon(ResourceBundle resourceBundle, String key) {
		String filename = getStringOptional(resourceBundle, key);
		if (!StringUtils.isBlank(filename)) {
			URL url = ResourceHelper.class.getResource(ICONS_DIRECTORY + "/" + filename);
			if (url != null) {
				return new ImageIcon(url);
			}
		}
		return null;
	}
	
	public static Icon getIcon(String filename) {
		filename = ICONS_DIRECTORY + "/" + filename;
		URL url = ResourceHelper.class.getResource(filename);
		if (url != null) {
			return new ImageIcon(url);
		} else {
			return null;
		}
	}
	
	public static Integer getInteger(ResourceBundle resourceBundle, String key) {
		try {
			String integerString = resourceBundle.getString(key);
			if (!StringUtils.isBlank(integerString)) {
				return Integer.parseInt(integerString);
			}
		} catch (MissingResourceException e) {
			// silent
		}
		return null;
	}
	
	public static KeyStroke getKeyStroke(ResourceBundle resourceBundle, String key) {
		String keyStrokeString = getStringOptional(resourceBundle, key);
		if (!StringUtils.isBlank(keyStrokeString)) {
			return KeyStroke.getKeyStroke(keyStrokeString);
		}
		return null;
	}
	
	public static Integer getKeyCode(ResourceBundle resourceBundle, String key) {
		String keyStrokeString = getStringOptional(resourceBundle, key);
		if (!StringUtils.isBlank(keyStrokeString)) {
			KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeString);
			if (keyStroke != null) {
				return keyStroke.getKeyCode();
			}
		}
		return null;
	}
	
	public static String getString(ResourceBundle resourceBundle, String key) {
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			if (!loggedMissings.contains(key)) {
				logger.log(Level.CONFIG, key + " missing", e);
				loggedMissings.add(key);
			}
			return "'" + key + "'";
		}
	}

	public static String getStringOptional(ResourceBundle resourceBundle, String key) {
		if (resourceBundle.containsKey(key)) {
			return resourceBundle.getString(key);
		} else {
			return null;
		}
	}

	public static String getApplicationTitle() {
		return ResourceHelper.getString(Resources.getResourceBundle(), "Application.title");
	}

	public static String getApplicationHomepage() {
		return ResourceHelper.getString(Resources.getResourceBundle(), "Application.homepage");
	}

	public static String getApplicationVendor() {
		return ResourceHelper.getString(Resources.getResourceBundle(), "Application.vendor");
	}

	public static String getApplicationVersion() {
		return ResourceHelper.getString(Resources.getResourceBundle(), "Application.version");
	}

	public static int getMnemonicIndex(String string) {
		if (string.length() < 2) {
			return -1;
		}
		if (string.charAt(0) == '&') {
			return 1;
		}
		string = string.replace("'&'", "_");
		for (int i = 1; i<string.length()-1; i++) {
			if (string.charAt(i-1) == '\'' && string.charAt(i+1) == '\'') {
				continue;
			}
			if (string.charAt(i) == '&') {
				return i+1;
			}
		}
		return -1;
	}

}

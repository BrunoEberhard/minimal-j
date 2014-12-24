package org.minimalj.frontend.swing;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.minimalj.util.resources.Resources;

public abstract class SwingResourceAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	
	protected SwingResourceAction() {
		String actionName = this.getClass().getSimpleName();
		initProperties(this, actionName);
	}

	protected SwingResourceAction(String actionName) {
		initProperties(this, actionName);
	}
	
	//
	
	public static Action initProperties(Action action, String baseName) {
		if (Resources.isAvailable(baseName)) {
			String text = Resources.getString(baseName);
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
		}
		
		Integer mnemonic = getKeyCode(baseName + ".mnemonic");
		if (mnemonic != null) {
			action.putValue(Action.MNEMONIC_KEY, mnemonic);
		}
		
		Integer index = Resources.getInteger(baseName + ".displayedMnemonicIndex", Resources.OPTIONAL);
		if (index != null) {
			action.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, index);
		}
		
		KeyStroke key = getKeyStroke(baseName + ".accelerator");
		if (key != null) {
			action.putValue(Action.ACCELERATOR_KEY, key);
		}
		
		Icon icon = Resources.getIconByResourceName(baseName + ".icon");
		if (icon != null) {
			action.putValue(Action.SMALL_ICON, icon);
			action.putValue(Action.LARGE_ICON_KEY, icon);
		}
		
		Icon smallIcon = Resources.getIconByResourceName(baseName + ".smallIcon");
		if (smallIcon != null) {
			action.putValue(Action.SMALL_ICON, smallIcon);
		}
		
		Icon largeIcon = Resources.getIconByResourceName(baseName + ".largeIcon");
		if (largeIcon != null) {
			action.putValue(Action.LARGE_ICON_KEY, largeIcon);
		}
		
		addValue(action, Action.SHORT_DESCRIPTION, baseName + ".shortDescription");
		addValue(action, Action.LONG_DESCRIPTION, baseName + ".longDescription");
		
		return action;
	}
	
	private static void addValue(Action action, String actionKey, String property) {
		if (Resources.isAvailable(property)) {
			action.putValue(actionKey, Resources.getString(property));
		}
	}

	public static KeyStroke getKeyStroke(String resourceName) {
		if (Resources.isAvailable(resourceName)) {
			String keyStrokeString = Resources.getString(resourceName);
			return KeyStroke.getKeyStroke(keyStrokeString);
		}
		return null;
	}
	
	public static Integer getKeyCode(String resourceName) {
		if (Resources.isAvailable(resourceName)) {
			String keyStrokeString = Resources.getString(resourceName);
			KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeString);
			if (keyStroke != null) {
				return keyStroke.getKeyCode();
			}
		}
		return null;
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

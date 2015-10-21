package org.minimalj.frontend.impl.swing;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.minimalj.util.StringUtils;
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
		String text = Resources.getString(baseName);
		action.putValue(Action.NAME, text);
		action.putValue(Action.MNEMONIC_KEY, keyCode(text));
		
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
		
		addValue(action, Action.SHORT_DESCRIPTION, baseName + ".description");
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
			String mnemonicString = Resources.getString(resourceName);
			return keyCode(mnemonicString);
		}
		return null;
	}
	
	private static Integer keyCode(String mnemonicString) {
		if (!StringUtils.isEmpty(mnemonicString)) {
			int mnemonicKey = mnemonicString.charAt(0);
			if (mnemonicKey >= 'a' && mnemonicKey <= 'z') {
				mnemonicKey -= ('a' - 'A');
			}
			return mnemonicKey;
		}
		return null;
	}

}

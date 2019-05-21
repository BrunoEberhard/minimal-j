package org.minimalj.model.validation;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.StringUtils;

/**
 * The text of this message should already be localized. You get the current Locale
 * from the LocaleContext class.
 *
 */
public class ValidationMessage {

	private String formattedText;
	private Object key;

	public ValidationMessage(Object key, String formattedText) {
		this.key = key;
		this.formattedText = formattedText;
	}

	public String getFormattedText() {
		return formattedText;
	}

	public Object getKey() {
		return key;
	}

	public PropertyInterface getProperty() {
		if (key instanceof PropertyInterface) {
			return (PropertyInterface) key;
		} else {
			return Keys.getProperty(key);
		}
	}
	
	@Override
	public String toString() {
		return "ValidationMessage [key=" + key + ", formattedText=" + formattedText + "]";
	}

	public static List<String> filterValidationMessage(List<ValidationMessage> validationMessages, PropertyInterface property) {
		List<String> filteredMessages = new ArrayList<>();
		if (validationMessages != null) {
			for (ValidationMessage validationMessage : validationMessages) {
				if (equalsOrParent(property, validationMessage.getProperty())) {
					filteredMessages.add(validationMessage.getFormattedText());
				}
			}
		}
		return filteredMessages;
	}

	private static boolean equalsOrParent(PropertyInterface p1, PropertyInterface p2) {
		if (p1 != null && p2 != null) {
			String path1 = p1.getPath();
			String path2 = p2.getPath();
			return StringUtils.equals(path1, path2) || path2.startsWith(path1) && path2.charAt(path1.length()) == '.';
		} else {
			return false;
		}
	}
	
	public static String formatHtml(List<ValidationMessage> validationMessages) {
		if (validationMessages != null && validationMessages.size() > 0) {
			StringBuilder s = new StringBuilder();
			s.append("<html>");
			for (int i = 0; i<validationMessages.size(); i++) {
				s.append(validationMessages.get(i).formattedText);
				if (i < validationMessages.size() - 1) {
					s.append("<br>");
				}
			}
			s.append("</html>");
			return s.toString();
		} else {
			return null;
		}
	}

}

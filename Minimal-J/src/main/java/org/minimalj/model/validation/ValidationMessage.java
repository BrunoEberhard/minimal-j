package org.minimalj.model.validation;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;

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
	
//	public static List<ValidationMessage> filterValidationMessage(List<ValidationMessage> validationMessages, PropertyInterface property) {
//		List<ValidationMessage> filteredMessages = Collections.emptyList();
//		if (validationMessages != null) {
//			for (ValidationMessage validationMessage : validationMessages) {
//				if (validationMessage.getProperty().equals(property)) {
//					if (filteredMessages.isEmpty()) {
//						filteredMessages = new ArrayList<ValidationMessage>();
//					}
//					filteredMessages.add(validationMessage);
//				}
//			}
//		}
//		return filteredMessages;
//	}

	public static List<String> filterValidationMessage(List<ValidationMessage> validationMessages, PropertyInterface property) {
		List<String> filteredMessages = new ArrayList<String>();
		if (validationMessages != null) {
			for (ValidationMessage validationMessage : validationMessages) {
				if (validationMessage.getProperty().equals(property)) {
					filteredMessages.add(validationMessage.getFormattedText());
				}
			}
		}
		return filteredMessages;
	}

	
	public static String formatHtml(List<ValidationMessage> validationMessages) {
		if (validationMessages.size() > 0) {
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

	public static String formatHtmlString(List<String> validationMessages) {
		if (validationMessages.size() > 0) {
			StringBuilder s = new StringBuilder();
			s.append("<html>");
			for (int i = 0; i<validationMessages.size(); i++) {
				s.append(validationMessages.get(i));
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

	public static String formatHtml(String validationMessage) {
		StringBuilder s = new StringBuilder();
		s.append("<html>");
		s.append(validationMessage);
		s.append("</html>");
		return s.toString();
	}

}

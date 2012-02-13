package ch.openech.mj.edit.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.openech.mj.db.model.Constants;
import ch.openech.mj.util.StringUtils;

public class ValidationMessage {

	private String formattedText;
	private String key;

	public ValidationMessage(Object key, String formattedText) {
		this(Constants.getConstant(key), formattedText);
	}
	
	public ValidationMessage(String key, String formattedText) {
		this.key = key;
		this.formattedText = formattedText;
	}

	public String getFormattedText() {
		return formattedText;
	}

	public String getKey() {
		return key;
	}

	@Override
	public String toString() {
		return "ValidationMessage [key=" + key + ", formattedText=" + formattedText + "]";
	}
	
	public static List<ValidationMessage> filterValidationMessage(List<ValidationMessage> validationMessages, String fieldName) {
		List<ValidationMessage> filteredMessages = Collections.emptyList();
		if (validationMessages != null) {
			for (ValidationMessage validationMessage : validationMessages) {
				if (StringUtils.equals(validationMessage.getKey(), fieldName)) {
					if (filteredMessages.isEmpty()) {
						filteredMessages = new ArrayList<ValidationMessage>();
					}
					filteredMessages.add(validationMessage);
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
	
}

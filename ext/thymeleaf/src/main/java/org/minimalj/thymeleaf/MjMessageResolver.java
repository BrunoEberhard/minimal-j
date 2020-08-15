package org.minimalj.thymeleaf;

import java.text.MessageFormat;
import java.util.Locale;

import org.minimalj.util.LocaleContext;
import org.minimalj.util.resources.Resources;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.messageresolver.AbstractMessageResolver;
import org.thymeleaf.util.Validate;

/**
 * Makes Minimal-J Resources accessible to Thymeleaf. Internal.
 * 
 * @author bruno
 */
public class MjMessageResolver extends AbstractMessageResolver {

	// copied from StandardMessageResolutionUtils
	private static final Object[] EMPTY_MESSAGE_PARAMETERS = new Object[0];

	@Override
	public String resolveMessage(ITemplateContext context, Class<?> origin, String key, Object[] messageParameters) {
		String message = Resources.getString(key);
		return formatMessage(LocaleContext.getCurrent(), message, messageParameters);
	}

	@Override
	// copied from StandardMessageResolver
	public String createAbsentMessageRepresentation(ITemplateContext context, Class<?> origin, String key, Object[] messageParameters) {
		Validate.notNull(key, "Message key cannot be null");
		if (context.getLocale() != null) {
			return "??" + key + "_" + context.getLocale().toString() + "??";
		}
		return "??" + key + "_" + "??";
	}

	// copied from StandardMessageResolutionUtils
	public static String formatMessage(Locale locale, String message, Object[] messageParameters) {
		if (message == null || !isFormatCandidate(message)) {
			return message;
		}
		MessageFormat messageFormat = new MessageFormat(message, locale);
		return messageFormat.format(messageParameters != null ? messageParameters : EMPTY_MESSAGE_PARAMETERS);
	}

	// copied from StandardMessageResolutionUtils
	private static boolean isFormatCandidate(String message) {
		int n = message.length();
		while (n-- != 0) {
			char c = message.charAt(n);
			if (c == '}' || c == '\'') {
				return true;
			}
		}
		return false;
	}
}

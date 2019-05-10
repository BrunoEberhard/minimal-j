package org.minimalj.frontend.impl.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.Page;
import org.minimalj.util.ExceptionUtils;

/**
 * Framework internal. The framework should be protected against exception
 * thrown by the application pages. The PageManagers could get into a invalid
 * state if for example getActions throws an Exception and the page is already
 * added to the list of displayed pages.
 *
 */
public class PageAccess {

	private static final Logger logger = Logger.getLogger(PageAccess.class.getName());

	public static IContent getContent(Page page) {
		try {
			return page.getContent();
		} catch (Exception exception) {
			ExceptionUtils.logReducedStackTrace(logger, exception);
			String text;
			if (Configuration.isDevModeActive()) {
				text = getStackTrace(exception);
				text = text.replace("\n", "<br>");
			} else {
				text = exception.toString();
			}
			return Frontend.getInstance().createHtmlContent("<html>" + text + "</html>");
		}
	}

	public static String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	public static List<Action> getActions(Page page) {
		try {
			return page.getActions();
		} catch (Exception exception) {
			ExceptionUtils.logReducedStackTrace(logger, exception);
			return Collections.emptyList();
		}
	}
	
}

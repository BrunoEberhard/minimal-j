package org.minimalj.frontend.impl.util;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

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
			String text = exception.getClass().getSimpleName() + ": " + exception.getMessage();
			return Frontend.getInstance().createHtmlContent("<html>" + text + "</html>");
		}
	}

	public static List<Action> getActions(Page page) {
		try {
			return page.getActions();
		} catch (Exception exception) {
			ExceptionUtils.logReducedStackTrace(logger, exception);
			return Collections.emptyList();
		}
	}

	public static String getRoute(Page page) {
		try {
			return page.getRoute();
		} catch (Exception exception) {
			ExceptionUtils.logReducedStackTrace(logger, exception);
			return null;
		}
	}
	
}

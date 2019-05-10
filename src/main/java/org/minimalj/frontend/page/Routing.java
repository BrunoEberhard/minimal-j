package org.minimalj.frontend.page;

import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.util.ExceptionUtils;

/**
 * Allows the Frontend to make a bookmark or a link for a Page. Note that the
 * user may see this String (for example in the URL). The parts of the route
 * should be glued together with '/'. For example "person/42". Although there is
 * no explicit limit to the length of the route it should stay human readable.
 * <p>
 * 
 * If the validateRoute method in this class doesn't accept the returned string
 * the route is ignored by the frontend.
 * 
 * @see org.minimalj.frontend.page.Page#validateRoute(String)
 */
public abstract class Routing {
	private static final Logger logger = Logger.getLogger(Routing.class.getName());

	private static final Routing routing = Application.getInstance().createRouting();

	public static final String getRouteSafe(Page page) {
		if (routing == null || page == null) {
			return null;
		}
		try {
			String route = routing.getRoute(page);
			if (Page.validateRoute(route)) {
				return route;
			} else {
				return null;
			}
		} catch (Exception exception) {
			ExceptionUtils.logReducedStackTrace(logger, exception);
			return null;
		}
	}

	public static final Page createPageSafe(String route) {
		if (routing == null || !Page.validateRoute(route)) {
			return null;
		}
		return routing.createPage(route);
	}

	public static boolean available() {
		return routing != null;
	}

	protected abstract String getRoute(Page page);

	protected abstract Page createPage(String route);

}

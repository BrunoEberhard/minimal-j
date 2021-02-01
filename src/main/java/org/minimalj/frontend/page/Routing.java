package org.minimalj.frontend.page;

import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.util.ExceptionUtils;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

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
			if (route != null && Page.validateRoute(route)) {
				return route;
			} else {
				return null;
			}
		} catch (Exception exception) {
			ExceptionUtils.logReducedStackTrace(logger, exception);
			return null;
		}
	}

	public static final /* NonNull */ Page createPageSafe(String route) {
		Page page = null;
		if (StringUtils.isEmpty(route) || route.equals(WebApplication.mjHandlerPath())) {
			page = new EmptyPage();
		} else if (routing != null && Page.validateRoute(route)) {
			try {
				page = routing.createPage(route);
			} catch (Exception exception) {
				ExceptionUtils.logReducedStackTrace(logger, exception);
				return new ExceptionPage(exception);
			}
		}
		if (page == null) {
			page = createNotAvailablePage(route);
		}
		return page;
	}

	private static Page createNotAvailablePage(String route) {
		return new HtmlPage(Resources.getString("NotAvailablePage.message")).title(Resources.getString("NotAvailablePage.title"));
	}

	public static boolean available() {
		return routing != null;
	}

	protected abstract String getRoute(Page page);

	/**
	 * @param route valid route string
	 * @return Page or <code>null</code> if route does not exist.
	 * @throws RuntimeException Don't try to catch everything in the implementation.
	 */
	protected abstract Page createPage(String route);

}

package org.minimalj.frontend.page;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.util.CloneHelper;
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

	private final LinkedHashMap<Class<? extends Page>, RoutingEntry<?, ? extends Page>> routeByPageClass = new LinkedHashMap<>(40);
	private final List<RoutingEntry<?, ? extends Page>> entries = new ArrayList<>();

	// TODO JDK 21 record
	public class RoutingEntry<T, P extends Page> {
		public String base;
		public Function<P, String> idProvider;
		public Function<String, ? extends Page> pageFactory;
		public Class<P> pageClass;
		public String navigation;
	}

	public <P extends Page> void register(String base, Class<P> pageClass) {
		register(base, pageClass, o -> CloneHelper.newInstance(pageClass));
	}

	public <P extends Page> void register(String base, Class<P> pageClass, Function<String, P> pageFactory) {
		register(base, pageClass, null, pageFactory, base);
	}
	
	public <T, P extends Page> void register(String base, Class<P> pageClass, Function<P, String> idProvider, Function<String, P> pageFactory, String navigation) {
		RoutingEntry<T, P> entry = new RoutingEntry<>();
		entry.base = Objects.requireNonNull(base);
		entry.pageClass = Objects.requireNonNull(pageClass);
		entry.idProvider = idProvider;
		entry.pageFactory = Objects.requireNonNull(pageFactory);
		entry.navigation = navigation;
		entries.add(entry);
		routeByPageClass.put(pageClass, entry);
	}

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

	public static final String getRouteSafe(Action action) {
		if (routing == null || action == null) {
			return null;
		}
		try {
			String route = null;
			if (action instanceof Routable) {
				Routable routable = (Routable) action;
				route = routable.getRoute();
			}
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
			page = Application.getInstance().createDefaultPage();
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

	protected String getRoute(Page page) {
		if (page instanceof Routable) {
			return ((Routable) page).getRoute();
		}
		for (RoutingEntry<?, ? extends Page> entry : entries) {
			if (entry.pageClass == page.getClass()) {
				if (entry.idProvider == null) {
					return "/" + entry.base;
				}
				Function idProvider = entry.idProvider;
				if (idProvider != null) {
					String id = (String) idProvider.apply(page);
					if (id != null) {
						return "/" + entry.base + "/" + id;	
					}
				}
			}
		}
		return null;
	}
	
	protected String getNavigation(Page page) {
		if (page instanceof Routable) {
			return ((Routable) page).getNavigationRoute();
		}
		for (RoutingEntry<?, ? extends Page> entry : entries) {
			if (entry.pageClass == page.getClass()) {
				if (entry.idProvider == null) {
					return "/" + entry.navigation;
				}
				Function idProvider = entry.idProvider;
				if (idProvider != null) {
					String id = (String) idProvider.apply(page);
					if (id != null) {
						return "/" + entry.navigation;	
					}
				}
			}
		}
		return null;
	}

	public static String navigation(Page page) {
		if (routing != null) {
			return routing.getNavigation(page);
		}
		return null;
	}

	/**
	 * @param route valid route string
	 * @return Page or <code>null</code> if route does not exist.
	 * @throws RuntimeException Don't try to catch everything in the implementation.
	 */
	protected Page createPage(String route) {
		while (route.startsWith("/")) {
			route = route.substring(1);
		}
		while (route.endsWith("/")) {
			route = route.substring(0, route.length() - 1);
		}
		if (route.length() == 0) {
			return Application.getInstance().createDefaultPage();
		}
		int index = route.indexOf('/');
		if (index >= 0) {
			String base = route.substring(0, index);
			String idString = route.substring(index + 1);
			for (RoutingEntry<?, ? extends Page> entry : entries) {
				if (entry.base.equals(base)) {
					Page page = entry.pageFactory.apply(idString);
					if (page != null) {
						return page;
					}
				}
			}
		} else {
			String base = route;
			return entries.stream().filter(e -> e.base.equals(base)).findFirst().map(e -> e.pageFactory.apply(null)).orElse(null);
		}
		return null;
	}

	public static interface Routable {
		public String getRoute();
		
		public default String getNavigationRoute() {
			return getRoute();
		}
	}
}

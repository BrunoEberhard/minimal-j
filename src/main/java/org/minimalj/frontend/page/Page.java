package org.minimalj.frontend.page;

import java.util.List;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;
import org.minimalj.security.AccessControl;
import org.minimalj.security.Authorization;
import org.minimalj.security.Subject;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

/**
 * Pages are one of the building blocks of an application. They are intended to fill the whole space
 * of a window (or simply the display space if the frontend has no windows). Their content is static
 * in contrast to dialogs which are meant to allow inputs.<p>
 * 
 * A page can have a title and a content. Pages are meant to be ContentProvider. Pages are
 * allocated a lot. They should be lightweight! The heavy stuff should be done when calling
 * getContent(). 
 *
 */
public abstract class Page implements AccessControl {
	
	public String getTitle() {
		return Resources.getString(getClass());
	}
	
	public abstract IContent getContent();
	
	/**
	 * 
	 * @return List of Action specific to this page and it's content. These
	 *         Actions can be displayed by the frontend as context menu or at
	 *         the right side of the page. Actions can be enabled or disabled
	 *         but not added or removed. Actions can be grouped with an
	 *         ActionGroup.
	 */
	public List<Action> getActions() {
		return null;
	}
	
	private static final String ALLOWED_CHARS = "-._~:/?#[]@!$&'()*+,;=%";

	/**
	 * Route String must obey some rules to be valid:
	 * <UL>
	 * <LI>start with a '/'</LI>
	 * <LI>no '/' at end</LI>
	 * <LI>contain no '..'</LI>
	 * <LI>all characters must be letter, digits or in ALLOWED_CHARS</LI>
	 * </UL>
	 * 
	 * @param route String provided by a page
	 * @return Frontend will accept route or not
	 * @see java.util.Base64#getUrlEncoder
	 */
	public static boolean validateRoute(String route) {
		if (StringUtils.isBlank(route)) {
			return true;
		}
		if (route.charAt(0) != '/' || route.length() > 1 && route.endsWith("/")) {
			return false;
		}
		if (route.contains("..")) {
			return false;
		}
		for (int i = 1; i < route.length(); i++) {
			char c = route.charAt(i);
			if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || ALLOWED_CHARS.indexOf(c) >= 0)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * This default implementation handles access by annotations. This can be
	 * overridden to grant access not only by the page class but also by the data
	 * the page will show.
	 * 
	 * @param subject the current subject
	 * @return true if the current subject can access this page.
	 */
	@Override
	public boolean hasAccess(Subject subject) {
		return !Boolean.FALSE.equals(Authorization.hasAccessByAnnotation(subject, getClass()));
	}
}
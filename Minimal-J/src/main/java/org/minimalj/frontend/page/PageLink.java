package org.minimalj.frontend.page;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.application.MjApplication;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.IAction;
import org.minimalj.util.ExceptionUtils;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;


public class PageLink implements IAction {
	private static final Logger logger = Logger.getLogger(PageLink.class.getName());

	private final String name;
	private final String link;
	private final boolean enabled;

	PageLink() {
		this(null);
	}
	
	PageLink(String name) {
		this(name, null);
	}

	public PageLink(String name, String link) {
		this(name, link, true);
	}

	public PageLink(String name, String link, boolean enabled) {
		this.name = name;
		this.link = link;
		this.enabled = enabled;
	}
	
	public PageLink(Class<? extends Page> pageClass, String... args) {
		this(Resources.getString(pageClass), link(pageClass, args));
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public String getLink() {
		return link;
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	@Override
	public void action() {
		ClientToolkit.getToolkit().show(link);
	}
	
	public static Page createPage(String pageLink) {
		if (StringUtils.isEmpty(pageLink)) {
			return MjApplication.getApplication().createDefaultPage();
		}
		try {
			int pos = pageLink.indexOf('/');
			String className = pos > 0 ? pageLink.substring(0, pos) : pageLink;
			String fullClassName;
			if (EmptyPage.class.getSimpleName().equals(className)) {
				fullClassName = EmptyPage.class.getName();
			} else {
				fullClassName = MjApplication.getCompletePackageName("frontend.page") + "." + className;
			}
			Class<?> clazz = Class.forName(fullClassName);
			if (pos > 0) {
				String[] fragmentParts = pageLink.substring(pos+1).split("/");
				if (fragmentParts.length > 1) {
					Class<?>[] argumentClasses = new Class[1];
					argumentClasses[0] = new String[0].getClass();
					return (Page) clazz.getConstructor(argumentClasses).newInstance(new Object[]{fragmentParts});
				} else {
					Class<?>[] argumentClasses = new Class[1];
					argumentClasses[0] = String.class;
					return (Page) clazz.getConstructor(argumentClasses).newInstance(fragmentParts[0]);
				}
			} else {
				Class<?>[] argumentClasses = new Class[0];
				return (Page) clazz.getConstructor(argumentClasses).newInstance();
			}
		} catch (Exception x) {
			String message = "Page could not be created: " + pageLink;
			if (x.getCause() instanceof Exception) {
				logger.log(Level.SEVERE, message);
				ExceptionUtils.logReducedStackTrace(logger, (Exception) x.getCause());
			} else {
				logger.log(Level.SEVERE, message, x);
			}
			// TODO It would be nice to have here an error page instead of an empty page
			return new EmptyPage();
		}
	}

	public static String link(Class<? extends Page> pageClass, String... args) {
		StringBuilder s = new StringBuilder();
		s.append(pageClass.getSimpleName());
		for (int i = 0; i<args.length; i++) {
			s.append("/"); s.append(args[i]);
		}
		return s.toString();
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public void setChangeListener(ActionChangeListener changeListener) {
		// n/a
	}
	
//	public static String link(String... args) {
//		if (args.length == 0) {
//			return "";
//		} else if (args.length == 1) {
//			return args[0];
//		} else {
//			StringBuilder s = new StringBuilder();
//			s.append(args[0]);
//			for (int i = 1; i<args.length; i++) {
//				s.append("/"); s.append(args[i]);
//			}
//			return s.toString();
//		}
//	}
	
}

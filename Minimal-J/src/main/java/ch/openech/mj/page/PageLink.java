package ch.openech.mj.page;

import java.util.logging.Level;
import java.util.logging.Logger;

import ch.openech.mj.application.MjApplication;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.IAction;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.util.StringUtils;


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
		this(Resources.getString(pageClass.getSimpleName() + ".text"), link(pageClass, args));
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
	public void action(IComponent context) {
		((PageContext) context).show(link);
	}
	
	public static Page createPage(PageContext context, String pageLink) {
		if (StringUtils.isEmpty(pageLink)) {
			return MjApplication.getApplication().createDefaultPage(context);
		}
		try {
			int pos = pageLink.indexOf('/');
			String className = pos > 0 ? pageLink.substring(0, pos) : pageLink;
			String fullClassName;
			if (EmptyPage.class.getSimpleName().equals(className)) {
				fullClassName = EmptyPage.class.getName();
			} else {
				fullClassName = MjApplication.getCompletePackageName("page") + "." + className;
			}
			Class<?> clazz = Class.forName(fullClassName);
			if (pos > 0) {
				String[] fragmentParts = pageLink.substring(pos+1).split("/");
				if (fragmentParts.length > 1) {
					Class<?>[] argumentClasses = new Class[2];
					argumentClasses[0] = PageContext.class;
					argumentClasses[1] = new String[0].getClass();
					return (Page) clazz.getConstructor(argumentClasses).newInstance(new Object[]{context, fragmentParts});
				} else {
					Class<?>[] argumentClasses = new Class[2];
					argumentClasses[0] = PageContext.class;
					argumentClasses[1] = String.class;
					return (Page) clazz.getConstructor(argumentClasses).newInstance(context, fragmentParts[0]);
				}
			} else {
				Class<?>[] argumentClasses = new Class[1];
				argumentClasses[0] = PageContext.class;
				return (Page) clazz.getConstructor(argumentClasses).newInstance(context);
			}
		} catch (Exception x) {
			logger.log(Level.SEVERE, "UriFragment Auflösung fehlgeschlagen: " + pageLink, x);
			// TODO It would be nice to have here an error page instead of an empty page
			return new EmptyPage(context);
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

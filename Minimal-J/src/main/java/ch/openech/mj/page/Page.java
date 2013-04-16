package ch.openech.mj.page;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;

import ch.openech.mj.application.MjApplication;
import ch.openech.mj.edit.EditorPage;
import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.util.StringUtils;

public abstract class Page {
	private static final Logger logger = Logger.getLogger(Page.class.getName());
	
	private final PageContext context;

	private String title;
	private Icon titleIcon;
	private String titleToolTip;
	
	public Page(PageContext context) {
		this.context = context;
		initProperties(Resources.getResourceBundle(), getResourceBaseName());
	}
	
	/**
	 * 
	 * @return true if this is a page that cannot be replaced by a new one. Used for editors.
	 */
	public boolean isExclusive() {
		return false;
	}
	
	protected PageContext getPageContext() {
		return context;
	}
	
	private void initProperties(ResourceBundle resourceBundle, String baseName) {
		title = ResourceHelper.getString(resourceBundle, baseName + ".Page.title");
		if (StringUtils.isBlank(title) || title.startsWith("!")) title = ResourceHelper.getString(resourceBundle, baseName + ".text");

		titleToolTip = ResourceHelper.getString(resourceBundle, baseName + ".Page.titleToolTip");

		titleIcon = ResourceHelper.getIcon(resourceBundle, baseName + ".Page.titleIcon");
	}
	
	/**
	 * Override to specify a special ResourceBaseName
	 * 
	 * @return the ResourceBaseName by naming convention. This would be the 
	 * (simple) class name without the ending Page
	 */
	protected String getResourceBaseName() {
		return resourceBaseNameByNamingConvention();
	}
	
	private String resourceBaseNameByNamingConvention() {
		String name = this.getClass().getSimpleName();
		if (name.endsWith("Page")) name = name.substring(0, name.length() - 4);
		return name;
	}

	public abstract IComponent getComponent();

	public void fillActionGroup(ActionGroup actionGroup) {
		// should be done in subclass
	}
	
	public String getTitle() {
		return title;
	}

	protected void setTitle(String title) {
		if (StringUtils.equals(this.title, title)) return;
		this.title = title;
		firePageChanged();
	}

	public Icon getTitleIcon() {
		return titleIcon;
	}

	protected void setTitleIcon(Icon titleIcon) {
		if (this.titleIcon == null && titleIcon == null || this.titleIcon != null && this.titleIcon.equals(titleIcon)) return;
		this.titleIcon = titleIcon;
		firePageChanged();
	}

	public String getTitleToolTip() {
		return titleToolTip;
	}

	protected void setTitleToolTip(String titleToolTip) {
		if (StringUtils.equals(this.titleToolTip, titleToolTip)) return;
		this.titleToolTip = titleToolTip;
		firePageChanged();
	}
	
	protected void show(Class<? extends Page> pageClass, String... args) {
		String pageLink = link(pageClass, args);
		context.show(pageLink);
	}
	
	private void firePageChanged() {
		if (context instanceof PageListener) {
			((PageListener) context).onPageTitleChanged(this);
		}
	}

	public interface PageListener {
		public void onPageTitleChanged(Page page);
	}
	
	public static Page createPage(PageContext context, String pageLink) {
		if (StringUtils.isEmpty(pageLink)) {
			return MjApplication.getApplication().createDefaultPage(context);
		}
		try {
			int pos = pageLink.indexOf('/');
			String className = pos > 0 ? pageLink.substring(0, pos) : pageLink;
			String fullClassName;
			if (EditorPage.class.getSimpleName().equals(className)) {
				fullClassName = EditorPage.class.getName();
			} else if (EmptyPage.class.getSimpleName().equals(className)) {
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
	
	public static String link(String... args) {
		if (args.length == 0) {
			return "";
		} else if (args.length == 1) {
			return args[0];
		} else {
			StringBuilder s = new StringBuilder();
			s.append(args[0]);
			for (int i = 1; i<args.length; i++) {
				s.append("/"); s.append(args[i]);
			}
			return s.toString();
		}
	}
	
}
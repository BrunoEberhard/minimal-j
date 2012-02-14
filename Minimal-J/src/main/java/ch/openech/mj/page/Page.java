package ch.openech.mj.page;

import java.util.ResourceBundle;

import javax.swing.Icon;

import ch.openech.mj.application.AsyncPage.PageWorkListener;
import ch.openech.mj.application.EmptyPage;
import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.util.StringUtils;


public abstract class Page {

	private PageContext context;

	private String title;
	private Icon titleIcon;
	private String titleToolTip;
	
	public Page() {
		initProperties(Resources.getResourceBundle(), getResourceBaseName());
	}
	
	/**
	 * @param context The context the page lives in. If the context implements PageListener 
	 * its informed when title of Page changes
	 */
	public void setPageContext(PageContext pageContext) {
		this.context = pageContext;
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
	
	public abstract IComponent getPanel();

	public void fillActionGroup(PageContext pageContext, ActionGroup actionGroup) {
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

	public void close() {
		if (context != null) {
			context.close();
		} else {
			throw new IllegalStateException("A Page without PageContext should not be visible and no close action should be performed");
		}
	}
	
	protected void show(Class<? extends Page> pageClass, String... args) {
		String pageLink = link(pageClass, args);
		context.show(pageLink);
	}
	
//	protected void showInNewTab(Page page) {
//		PageContext newPageContext = context.addTab();
//		page.setPageContext(newPageContext);
//		newPageContext.show(page);
//	}
	
	private void firePageChanged() {
		if (context instanceof PageListener) {
			((PageListener) context).onPageTitleChanged(this);
		}
	}

	protected void firePageWorkStart(String workName) {
		if (context instanceof PageWorkListener) {
			((PageWorkListener) context).onPageWorkStart(workName);
		}
	}

	protected void firePageWorkEnd() {
		if (context instanceof PageWorkListener) {
			((PageWorkListener) context).onPageWorkEnd();
		}
	}
	
	public interface PageListener {
		public void onPageTitleChanged(Page page);
	}
	
	public static Page createPage(String pageLink) {
		try {
			if (!StringUtils.isEmpty(pageLink)) {
				int pos = pageLink.indexOf('/');
				String className = pos > 0 ? pageLink.substring(0, pos) : pageLink;
				Class<?> clazz = Class.forName(className);
				if (pos > 0) {
					String[] fragmentParts = pageLink.substring(pos+1).split("/");
					if (fragmentParts.length > 1) {
						Class<?>[] argumentClasses = new Class[1];
						argumentClasses[0] = new String[0].getClass();
						return (Page) clazz.getConstructor(argumentClasses).newInstance(new Object[]{fragmentParts});
					} else {
						return (Page) clazz.getConstructor(String.class).newInstance(fragmentParts[0]);
					}
				} else {
					return (Page) clazz.newInstance();
				}
			}
		} catch (Exception x) {
			throw new RuntimeException("UriFragment Auflösung fehlgeschlagen", x);
		}
		return new EmptyPage();
	}


	public static String link(Class<? extends Page> pageClass, String... args) {
		StringBuilder s = new StringBuilder();
		s.append(pageClass.getName());
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
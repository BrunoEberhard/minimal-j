package org.minimalj.test;

import java.util.List;

import org.junit.Assert;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.resources.Resources;

public interface FrameTestFacade {

	public interface LoginFrameFacade {

		public boolean hasSkipLogin();

		public boolean hasClose();

		public void login();

		public void cancel();

		public void close();
	}

	public interface UserPasswordLoginTestFacade extends LoginFrameFacade {

		public void setUser(String name);

		public void setPassword(String password);

	}

	public interface PageContainerTestFacade extends FrameTestFacade {

		public NavigationTestFacade getNavigation();

		public List<PageTestFacade> getPages();

		public DialogTestFacade getDialog();

		public ActionTestFacade getBack();

		public ActionTestFacade getForward();
		
		public default PageTestFacade getPage() {
			List<PageTestFacade> pages = getPages();
			Assert.assertEquals("Exact one page should be visible", 1, pages.size());
			return pages.get(0);
		}

	}

	public interface ActionTestFacade extends Runnable {

		public boolean isEnabled();
	}

	public interface NavigationTestFacade {

		public Runnable get(String text);

	}
	
	public interface PageTestFacade {

		public String getTitle();
		
		public NavigationTestFacade getContextMenu();
		
		// Query
		
		public void executeQuery(String query);
				
		// Table
		
		public TableTestFacade getTable();
		
		// Form

		public FormTestFacade getForm();
		
		// Html

		public boolean contains(String string);

	}

	public interface DialogTestFacade {
		
		public FormTestFacade getForm();
		
		public ActionTestFacade getAction(String caption);
		
		public default void save() {
			String caption = Resources.getString("SaveAction");
			ActionTestFacade action = getAction(caption);
			action.run();
		}
		
	}
	
	public interface FormTestFacade {

		public FormElementTestFacade getElement(String caption);

		public default FormElementTestFacade element(Object key) {
			PropertyInterface property = Keys.getProperty(key);
			String caption = Resources.getPropertyName(property);
			return getElement(caption);
		}
	}
	
	public interface FormElementTestFacade {

		public String getText();

		public void setText(String value);
		
		public String getValidation();

		public SearchTableTestFacade lookup();

		public String getLine(int line);

		public List<ActionTestFacade> getLineActions(int line);
	}

	public interface TableTestFacade {

		public int getColumnCount();

		public int getRowCount();
		
		public String getHeader(int column);
		
		public String getValue(int row, int column);
		
		public void activate(int row);
	}
	
	public interface SearchTableTestFacade extends TableTestFacade {
		
		public void search(String text);
	}
	
	/*

	public static abstract class PageTestFacade {
		public final String title;
		
		public PageTestFacade(String title) {
			this.title = title;
		}
		
		public String getTitle() {
			return title;
		}

	}

	public static abstract class TablePageTestFacade extends PageTestFacade {
		
		public TablePageTestFacade(String title) {
			super(title);
		}

		public abstract String getColumns();
	}

	public static abstract class QueryPageTestFacade extends PageTestFacade {
		
		public QueryPageTestFacade(String title) {
			super(title);
		}

		public abstract void executeQuery(String query);
	}
	
	public static class TextPageTestFacade extends PageTestFacade {
		public final String text;
		
		public TextPageTestFacade(String title, String text) {
			super(title);
			this.text = text;
		}
		
		public boolean contains(String string) {
			return text.contains(string);
		}
	}

	
	public static abstract class FormPageTestFacade extends PageTestFacade {

		public FormPageTestFacade(String title) {
			super(title);
		}

	}

	*/
}
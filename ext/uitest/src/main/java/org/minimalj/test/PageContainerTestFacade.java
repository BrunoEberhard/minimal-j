package org.minimalj.test;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.Property;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

public interface PageContainerTestFacade {
	
	public NavigationTestFacade getNavigation();

	public List<PageTestFacade> getPages();

	public DialogTestFacade getDialog();

	public ActionTestFacade getBack();

	public ActionTestFacade getForward();

	public default boolean hasLogout() {
		return false; // TODO implement in Swing Frontend
	}

	public default void logout() {
		// TODO implement in Swing Frontend
	}

	public default PageTestFacade getPage() {
		List<PageTestFacade> pages = getPages();
		Assertions.assertEquals(1, pages.size(), "Exact one page should be visible");
		return pages.get(0);
	}
	
	public default PageTestFacade page() {
		List<PageTestFacade> pages = getPages();
		return pages.get(pages.size()-1);
	}

	public interface ActionTestFacade extends Runnable {

		public boolean isEnabled();
	}

	public interface NavigationTestFacade {

		public Runnable get(String text);

		public default void run(String text) {
			Runnable runnable = get(text);
			Assertions.assertNotNull(runnable, "There should be a navigation action '" + text + "'");
			runnable.run();
		}
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
		
		public void close();
		
		public FormTestFacade getForm();
		
		public TableTestFacade getTable();
		
		public SearchTableTestFacade getSearchTable();

		public ActionTestFacade getAction(String caption);
		
		public default void save() {
			String caption = Resources.getString("SaveAction");
			ActionTestFacade action = getAction(caption);
			action.run();
		}
		
		public default FormTestFacade form() {
			return getForm();
		}
	}
	
	public interface FormTestFacade {

		public FormElementTestFacade getElement(String caption);

		public default FormElementTestFacade getElement(String caption, int index) {
			return null;
		}

		
		public default FormElementTestFacade getElement(int row, int column) {
			return null;
		}
		
		public default ActionTestFacade getAction(String label) {
			return null;
		}

		public default FormElementTestFacade element(Object key) {
			Property property = Keys.getProperty(key);
			String caption = Resources.getPropertyName(property);
			return getElement(caption);
		}
		
		public default void assertMandatory(Object key) {
			element(key).setText("");
			Assertions.assertNotNull(element(key).getValidation(), Resources.getPropertyName(Keys.getProperty(key)) + " must be mandatory");
		}
		
		public default void set(String caption, String value) {
			FormElementTestFacade element = getElement(caption);
			Assertions.assertNotNull(element, "There should be a FormElement with caption'" + caption + "'");
			element.setText(value);
		}

		public default void set(String caption, boolean checked) {
			FormElementTestFacade element = getElement(caption);
			Assertions.assertNotNull(element, "There should be a FormElement with caption'" + caption + "'");
			element.setChecked(checked);
		}
	}
	
	public interface FormElementTestFacade {

		public String getText();

		public void setText(String value);
		
		public String getValidation();

		public void lookup();
		
		public default void action(String text) {
			//TODO
		}
		
		public List<String> getComboBoxValues();
		
		public String getLine(int line);

		public List<ActionTestFacade> getLineActions(int line);
		
		public default FormElementTestFacade groupItem(int pos) {
			return null;
		}

		public default boolean isChecked() {
			return false;
		}

		public default void setChecked(boolean checked) {
			// TODO
		}
		
		// only allowed if FormElement is a TableFormElement
		public default FormTestFacade row(int pos) {
			return null; // TODO
		}

	}

	public interface TableTestFacade {

		public int getColumnCount();

		public int getRowCount();
		
		public String getHeader(int column);
		
		public String getValue(int row, int column);

		public default void activate(int row, int column) {
			// TODO
		}

		public void activate(int row);
		
		public default void select(int row) {
			// TODO
		}
		
		public default FormTestFacade getOverview() {
			return null; // TODO
		}
		
		public void setFilterVisible(boolean visible);
		
		public void setFilter(int column, String filterString);
		
		public String getFilter(int column);
		
		public DialogTestFacade filterLookup(int column);
		
		public default int findRow(String text) {
			for (int row = 0; row < getRowCount(); row++) {
				for (int column = 0; column < getColumnCount(); column++) {
					String value = getValue(row, column);
					if (StringUtils.equals(text, value)) {
						return row;
					}
				}
			}
			return -1;
		}

		public default int findRow(String text, int column) {
			for (int row = 0; row < getRowCount(); row++) {
				String value = getValue(row, column);
				if (StringUtils.equals(text, value)) {
					return row;
				}
			}
			return -1;
		}

		public default int row(String text, Object key) {
			int column = column(key);
			return findRow(text, column);
		}

		public default int findColumn(String text) {
			for (int column = 0; column < getColumnCount(); column++) {
				String value = getHeader(column);
				if (StringUtils.equals(text, value)) {
					return column;
				}
			}
			return -1;
		}
		
		public default int column(Object key) {
			String text = Resources.getPropertyName(Keys.getProperty(key));
			return findColumn(text);
		}

		public default boolean isFilterVisible() {
			return false;
		}
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

	public static abstract class TablePageTestFacade implements PageTestFacade {
		
		public TablePageTestFacade(String title) {
			super(title);
		}

		public abstract String getColumns();
	}

	public static abstract class QueryPageTestFacade implements PageTestFacade {
		
		public QueryPageTestFacade(String title) {
			super(title);
		}

		public abstract void executeQuery(String query);
	}
	
	public static class TextPageTestFacade implements PageTestFacade {
		public final String text;
		
		public TextPageTestFacade(String title, String text) {
			super(title);
			this.text = text;
		}
		
		public boolean contains(String string) {
			return text.contains(string);
		}
	}

	
	public static abstract class FormPageTestFacade implements PageTestFacade {

		public FormPageTestFacade(String title) {
			super(title);
		}

	}

	*/
}

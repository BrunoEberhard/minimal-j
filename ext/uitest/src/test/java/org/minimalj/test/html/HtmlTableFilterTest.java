package org.minimalj.test.html;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.minimalj.application.Application;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.EmptyPage;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageAction;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.model.Keys;
import org.minimalj.test.PageContainerTestFacade;
import org.minimalj.test.PageContainerTestFacade.DialogTestFacade;
import org.minimalj.test.PageContainerTestFacade.FormElementTestFacade;
import org.minimalj.test.PageContainerTestFacade.PageTestFacade;
import org.minimalj.test.PageContainerTestFacade.TableTestFacade;
import org.minimalj.test.TestUtil;
import org.minimalj.test.web.WebTest;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

public class HtmlTableFilterTest extends WebTest {

	@AfterEach
	public void cleanup() {
		TestUtil.shutdown();
	}

	@BeforeEach
	public void startApplication() {
		start(new TableFilterTestApplication());
	}	

	@Test
	public void testFilterDate() {
		PageContainerTestFacade pageContainer = ui().getCurrentPageContainerTestFacade();

		PageTestFacade page = pageContainer.getPage();

		TableTestFacade table = page.getTable();
		table.setFilterVisible(true);

		table.setFilter(0, "8.10.2020");
		Assertions.assertEquals(1, table.getRowCount());

		table.setFilter(0, "7.10.2020");
		Assertions.assertEquals(0, table.getRowCount());

		table.setFilter(0, "> 7.10.2020");
		Assertions.assertEquals(1, table.getRowCount());

		table.setFilter(0, "< 7.10.2020");
		Assertions.assertEquals(0, table.getRowCount());

		table.setFilter(0, ">9.10.2020");
		Assertions.assertEquals(0, table.getRowCount());

		table.setFilter(0, "<9.10.2020");
		Assertions.assertEquals(1, table.getRowCount());

		table.setFilter(0, "7.10.2020-9.10.2020");
		Assertions.assertEquals(1, table.getRowCount());

		table.setFilter(0, "9.10.2020-11.10.2020");
		Assertions.assertEquals(0, table.getRowCount());

		table.setFilter(0, "9.13.2020");
		Assertions.assertEquals(1, table.getRowCount());

		table.setFilter(0, "9.13.2020-11.10.2020");
		Assertions.assertEquals(1, table.getRowCount());

		table.setFilter(0, "2020");
		Assertions.assertEquals(1, table.getRowCount());

		table.setFilter(0, "2021");
		Assertions.assertEquals(0, table.getRowCount());
	}

	@Test
	public void testFilterDateDialog() {
		PageContainerTestFacade pageContainer = ui().getCurrentPageContainerTestFacade();

		PageTestFacade page = pageContainer.getPage();

		TableTestFacade table = page.getTable();
		table.setFilterVisible(true);

		table.setFilter(0, "8.10.2020");
		DialogTestFacade dialog = table.filterLookup(0);

		FormElementTestFacade filterSelectionElement = dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filter"));
		Assertions.assertEquals(Resources.getString("EqualsFilterPredicate"), filterSelectionElement.getText());

		FormElementTestFacade filterStringElement = dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filterValue"));
		Assertions.assertEquals("8.10.2020", filterStringElement.getText());

		filterStringElement.setText("9.10.2020");
		dialog.save();

		Assertions.assertEquals("9.10.2020", table.getFilter(0));
		Assertions.assertEquals(0, table.getRowCount());

		//

		table.setFilter(0, "<7.10.2020");
		dialog = table.filterLookup(0);

		filterSelectionElement = dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filter"));
		Assertions.assertEquals(Resources.getString("MaxFilterPredicate"), filterSelectionElement.getText());

		filterSelectionElement.setText(Resources.getString("MinFilterPredicate"));
		filterStringElement = dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filterValue"));
		Assertions.assertEquals("7.10.2020", filterStringElement.getText());

		dialog.save();

		Assertions.assertEquals("> 7.10.2020", table.getFilter(0));
		Assertions.assertEquals(1, table.getRowCount());

		//

		dialog = table.filterLookup(0);
		filterSelectionElement = dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filter"));
		filterSelectionElement.setText(Resources.getString("RangeFilterPredicate"));

		FormElementTestFacade filterStringElement1 = dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filterValue1"));
		FormElementTestFacade filterStringElement2 = dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filterValue2"));

		Assertions.assertEquals("7.10.2020", filterStringElement1.getText());
		Assertions.assertEquals("", filterStringElement2.getText());

		filterStringElement1.setText("6.10.2020");
		filterStringElement2.setText("8.10.2020");
		dialog.save();

		Assertions.assertEquals("6.10.2020 - 8.10.2020", table.getFilter(0));
		Assertions.assertEquals(1, table.getRowCount());
		
		//
		
		dialog = table.filterLookup(0);
		filterSelectionElement = dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filter"));

		filterStringElement1 = dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filterValue1"));
		filterStringElement2 = dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filterValue2"));

		filterStringElement1.setText("1.13.2020");
		Assertions.assertFalse(dialog.getAction(Resources.getString("SaveAction")).isEnabled());
 		Assertions.assertFalse(StringUtils.isEmpty(dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filterValue1")).getValidation()));
 		
 		dialog.getAction(Resources.getString("CancelAction")).run();
	}
	
	@Test
	public void testFilterDialogOperators() {
		PageContainerTestFacade pageContainer = ui().getCurrentPageContainerTestFacade();

		PageTestFacade page = pageContainer.getPage();

		TableTestFacade table = page.getTable();
		table.setFilterVisible(true);

		DialogTestFacade dialog = table.filterLookup(0);
		FormElementTestFacade filterStringElement = dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filterValue"));
		Assertions.assertEquals("", filterStringElement.getText());

		dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filter")).setText(Resources.getString("MaxFilterPredicate"));
		filterStringElement = dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filterValue"));
		Assertions.assertEquals("", filterStringElement.getText());

		dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filter")).setText(Resources.getString("MinFilterPredicate"));
		filterStringElement = dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filterValue"));
		Assertions.assertEquals("", filterStringElement.getText());

		dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filter")).setText(Resources.getString("RangeFilterPredicate"));
		FormElementTestFacade filterStringElement1 = dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filterValue1"));
		FormElementTestFacade filterStringElement2 = dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filterValue2"));
		Assertions.assertEquals("", filterStringElement1.getText());
		Assertions.assertEquals("", filterStringElement2.getText());
		
		filterStringElement1.setText("6.10.2020");
		dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filter")).setText(Resources.getString("MinFilterPredicate"));
		filterStringElement = dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filterValue"));
		Assertions.assertEquals("6.10.2020", filterStringElement.getText());

		dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filter")).setText(Resources.getString("MaxFilterPredicate"));
		filterStringElement = dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filterValue"));
		Assertions.assertEquals("6.10.2020", filterStringElement.getText());

		dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filter")).setText(Resources.getString("EqualsFilterPredicate"));
		filterStringElement = dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filterValue"));
		Assertions.assertEquals("6.10.2020", filterStringElement.getText());

		dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filter")).setText(Resources.getString("RangeFilterPredicate"));
		filterStringElement1 = dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filterValue1"));
		filterStringElement2 = dialog.getForm().getElement(Resources.getString("ColumnFilterModel.filterValue2"));
		Assertions.assertEquals("6.10.2020", filterStringElement1.getText());
		Assertions.assertEquals("", filterStringElement2.getText());
	}
	
	@Test
	public void testFilterAfterNavigation() {
		PageContainerTestFacade pageContainer = ui().getCurrentPageContainerTestFacade();

		PageTestFacade page = pageContainer.getPage();

		TableTestFacade table = page.getTable();
		table.setFilterVisible(true);
		table.setFilter(0, "2.10.2020");
		pageContainer.getNavigation().get("Empty").run();
		pageContainer.getBack().run();

		page = pageContainer.getPage();
		table = page.getTable();
		Assertions.assertEquals(0, table.getRowCount(), "When navigating back to a table with set filter the filter should be active");
		Assertions.assertEquals("2.10.2020", table.getFilter(0), "When navigating back to a table with set filter the filter should be keep value");
		Assertions.assertTrue(table.isFilterVisible(), "When navigating back to a table with set filter the filter should be visible");
	}

	public static class TableFilterTestApplication extends Application {

		@Override
		public Page createDefaultPage() {
			return new TableFilterTestPage();
		}
		
		@Override
		public List<Action> getNavigation() {
			return List.of(new PageAction(new EmptyPage(), "Empty"));
		}
	}

	public static class TableFilterTestEntity {

		public static final TableFilterTestEntity $ = Keys.of(TableFilterTestEntity.class);

		public LocalDate date;

		public LocalDateTime dateTime;
	}

	public static class TableFilterTestPage extends TablePage<TableFilterTestEntity> {

		@Override
		protected Object[] getColumns() {
			return new Object[] { TableFilterTestEntity.$.date, TableFilterTestEntity.$.dateTime };
		}

		@Override
		protected List<TableFilterTestEntity> load() {
			TableFilterTestEntity entity1 = new TableFilterTestEntity();
			entity1.date = LocalDate.of(2020, 10, 8);
			entity1.dateTime = LocalDateTime.of(2020, 10, 8, 10, 11);

			return new ArrayList<>(List.of(entity1));
		}

	}
}

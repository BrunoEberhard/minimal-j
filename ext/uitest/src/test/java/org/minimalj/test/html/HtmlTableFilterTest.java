package org.minimalj.test.html;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.minimalj.application.Application;
import org.minimalj.frontend.impl.util.ColumnFilterEditor.ColumnFilterModel;
import org.minimalj.frontend.page.Page;
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

	@After
	public void cleanup() {
		TestUtil.shutdown();
	}

	@Before
	public void startApplication() {
		start(new TableFilterTestApplication());
	}

	@Test
	public void testFilterDate() {
		PageContainerTestFacade pageContainer = ui().getCurrentPageContainerTestFacade();

		PageTestFacade page = pageContainer.getPage();

		TableTestFacade table = page.getTable();
		table.setFilterActive(true);

		table.setFilter(0, "8.10.2020");
		Assert.assertEquals(1, table.getRowCount());

		table.setFilter(0, "7.10.2020");
		Assert.assertEquals(0, table.getRowCount());

		table.setFilter(0, "> 7.10.2020");
		Assert.assertEquals(1, table.getRowCount());

		table.setFilter(0, "< 7.10.2020");
		Assert.assertEquals(0, table.getRowCount());

		table.setFilter(0, ">9.10.2020");
		Assert.assertEquals(0, table.getRowCount());

		table.setFilter(0, "<9.10.2020");
		Assert.assertEquals(1, table.getRowCount());

		table.setFilter(0, "7.10.2020-9.10.2020");
		Assert.assertEquals(1, table.getRowCount());

		table.setFilter(0, "9.10.2020-11.10.2020");
		Assert.assertEquals(0, table.getRowCount());

		table.setFilter(0, "9.13.2020");
		Assert.assertEquals(1, table.getRowCount());

		table.setFilter(0, "9.13.2020-11.10.2020");
		Assert.assertEquals(1, table.getRowCount());

		table.setFilter(0, "2020");
		Assert.assertEquals(1, table.getRowCount());

		table.setFilter(0, "2021");
		Assert.assertEquals(0, table.getRowCount());
	}

	@Test
	public void testFilterDateDialog() {
		PageContainerTestFacade pageContainer = ui().getCurrentPageContainerTestFacade();

		PageTestFacade page = pageContainer.getPage();

		TableTestFacade table = page.getTable();
		table.setFilterActive(true);

		table.setFilter(0, "8.10.2020");
		DialogTestFacade dialog = table.filterLookup(0);

		FormElementTestFacade filterSelectionElement = dialog.getForm().element(ColumnFilterModel.$.filter);
		Assert.assertEquals(Resources.getString("EqualsFilterPredicate"), filterSelectionElement.getText());

		FormElementTestFacade filterStringElement = dialog.getForm().element(ColumnFilterModel.$.filterString);
		Assert.assertEquals("8.10.2020", filterStringElement.getText());

		filterStringElement.setText("9.10.2020");
		dialog.save();

		Assert.assertEquals("9.10.2020", table.getFilter(0));
		Assert.assertEquals(0, table.getRowCount());

		//

		table.setFilter(0, "<7.10.2020");
		dialog = table.filterLookup(0);

		filterSelectionElement = dialog.getForm().element(ColumnFilterModel.$.filter);
		Assert.assertEquals(Resources.getString("MaxFilterPredicate"), filterSelectionElement.getText());

		filterSelectionElement.setText(Resources.getString("MinFilterPredicate"));
		filterStringElement = dialog.getForm().element(ColumnFilterModel.$.filterString);
		Assert.assertEquals("7.10.2020", filterStringElement.getText());

		dialog.save();

		Assert.assertEquals("> 7.10.2020", table.getFilter(0));
		Assert.assertEquals(1, table.getRowCount());

		//

		dialog = table.filterLookup(0);
		filterSelectionElement = dialog.getForm().element(ColumnFilterModel.$.filter);
		filterSelectionElement.setText(Resources.getString("RangeFilterPredicate"));

		FormElementTestFacade filterStringElement1 = dialog.getForm().element(ColumnFilterModel.$.filterString).groupItem(0);
		FormElementTestFacade filterStringElement2 = dialog.getForm().element(ColumnFilterModel.$.filterString).groupItem(2);

		Assert.assertEquals("7.10.2020", filterStringElement1.getText());
		Assert.assertEquals("", filterStringElement2.getText());

		filterStringElement1.setText("6.10.2020");
		filterStringElement2.setText("8.10.2020");
		dialog.save();

		Assert.assertEquals("6.10.2020 - 8.10.2020", table.getFilter(0));
		Assert.assertEquals(1, table.getRowCount());
		
		//
		
		dialog = table.filterLookup(0);
		filterSelectionElement = dialog.getForm().element(ColumnFilterModel.$.filter);

		filterStringElement1 = dialog.getForm().element(ColumnFilterModel.$.filterString).groupItem(0);
		filterStringElement2 = dialog.getForm().element(ColumnFilterModel.$.filterString).groupItem(2);

		filterStringElement1.setText("1.13.2020");
		Assert.assertFalse(dialog.getAction(Resources.getString("SaveAction")).isEnabled());
 		Assert.assertFalse(StringUtils.isEmpty(dialog.getForm().element(ColumnFilterModel.$.filterString).getValidation()));
 		
 		dialog.getAction(Resources.getString("CancelAction")).run();
	}
	
	@Test
	public void testFilterDialogOperators() {
		PageContainerTestFacade pageContainer = ui().getCurrentPageContainerTestFacade();

		PageTestFacade page = pageContainer.getPage();

		TableTestFacade table = page.getTable();
		table.setFilterActive(true);

		DialogTestFacade dialog = table.filterLookup(0);
		FormElementTestFacade filterStringElement = dialog.getForm().element(ColumnFilterModel.$.filterString);
		Assert.assertEquals("", filterStringElement.getText());

		FormElementTestFacade filterSelectionElement = dialog.getForm().element(ColumnFilterModel.$.filter);

		filterSelectionElement.setText(Resources.getString("MaxFilterPredicate"));
		filterStringElement = dialog.getForm().element(ColumnFilterModel.$.filterString);
		Assert.assertEquals("", filterStringElement.getText());

		filterSelectionElement.setText(Resources.getString("MinFilterPredicate"));
		filterStringElement = dialog.getForm().element(ColumnFilterModel.$.filterString);
		Assert.assertEquals("", filterStringElement.getText());

		filterSelectionElement.setText(Resources.getString("RangeFilterPredicate"));
		FormElementTestFacade filterStringElement1 = dialog.getForm().element(ColumnFilterModel.$.filterString).groupItem(0);
		FormElementTestFacade filterStringElement2 = dialog.getForm().element(ColumnFilterModel.$.filterString).groupItem(2);
		Assert.assertEquals("", filterStringElement1.getText());
		Assert.assertEquals("", filterStringElement2.getText());
		
		filterStringElement1.setText("6.10.2020");
		filterSelectionElement.setText(Resources.getString("MinFilterPredicate"));
		filterStringElement = dialog.getForm().element(ColumnFilterModel.$.filterString);
		Assert.assertEquals("6.10.2020", filterStringElement.getText());

		filterSelectionElement.setText(Resources.getString("MaxFilterPredicate"));
		filterStringElement = dialog.getForm().element(ColumnFilterModel.$.filterString);
		Assert.assertEquals("6.10.2020", filterStringElement.getText());

		filterSelectionElement.setText(Resources.getString("EqualsFilterPredicate"));
		filterStringElement = dialog.getForm().element(ColumnFilterModel.$.filterString);
		Assert.assertEquals("6.10.2020", filterStringElement.getText());

		filterSelectionElement.setText(Resources.getString("RangeFilterPredicate"));
		filterStringElement1 = dialog.getForm().element(ColumnFilterModel.$.filterString).groupItem(0);
		filterStringElement2 = dialog.getForm().element(ColumnFilterModel.$.filterString).groupItem(2);
		Assert.assertEquals("6.10.2020", filterStringElement1.getText());
		Assert.assertEquals("", filterStringElement2.getText());
	}


	public static class TableFilterTestApplication extends Application {

		@Override
		public Page createDefaultPage() {
			return new TableFilterTestPage();
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

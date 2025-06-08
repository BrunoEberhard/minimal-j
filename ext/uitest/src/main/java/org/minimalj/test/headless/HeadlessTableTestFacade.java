package org.minimalj.test.headless;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.impl.json.JsonComponent;
import org.minimalj.frontend.impl.json.JsonFormContent;
import org.minimalj.frontend.impl.json.JsonLookup;
import org.minimalj.frontend.impl.json.JsonTable;
import org.minimalj.frontend.impl.json.JsonTable.JsonTableModel;
import org.minimalj.test.PageContainerTestFacade.FormTestFacade;
import org.minimalj.test.PageContainerTestFacade.SearchTableTestFacade;
import org.minimalj.test.PageContainerTestFacade.TableTestFacade;

public class HeadlessTableTestFacade implements TableTestFacade {
	final JsonTable<?> table;
	final JsonFormContent overview;
	final HeadlessFormTestFacade overviewTestFacade;
	final Map<Integer, String> filters = new HashMap<>();
	
	public HeadlessTableTestFacade(JsonTable<?> table) {
		this(table, null);
	}
	
	public HeadlessTableTestFacade(JsonTable<?> table, JsonFormContent overview) {
		this.table = table;
		this.overview = overview;
		this.overviewTestFacade = overview != null ? new HeadlessFormTestFacade(overview) : null;
	}
	
	public JsonTableModel getTableModel() {
		return ((JsonTableModel) table.get("tableModel"));
	}
	
	public List<String> getHeaders() {
		return (List<String>) getTableModel().get("headers");
	}

	public IComponent[] getHeaderFilters() {
		return (IComponent[]) getTableModel().get("headerFilters");
	}

	public List<List> getContent() {
		return (List<List>) table.get("tableContent");
	}
	
	@Override
	public int getColumnCount() {
		return getHeaders().size();
	}

	@Override
	public int getRowCount() {
		return getContent().size();
	}

	@Override
	public String getHeader(int column) {
		return getHeaders().get(column);
	}

	@Override
	public String getValue(int row, int column) {
		var cell = getContent().get(row).get(column);
		if (cell instanceof String string) {
			return string;
		} else if (cell instanceof Map map) {
			return (String)map.get("value");
		}
		return null;
	}

	@Override
	public void activate(int row) {
		table.action(row);
	}
	
	@Override
	public void activate(int row, int column) {
		table.cellAction(row, column);
	}
	
	@Override
	public void select(int row) {
		List<Number> rows = List.of(row);
		table.selection(rows);
	}

	@Override
	public FormTestFacade getOverview() {
		return overviewTestFacade;
	}
	
	@Override
	public void setFilterVisible(boolean visible) {
		table.setFilterVisible(visible);
	}
	
	@Override
	public boolean isFilterVisible() {
		var tableModel = (JsonTable<?>.JsonTableModel) table.get("tableModel");
		return tableModel.isFilterVisible();
	}
	
	private JsonComponent getFilterComponent(int column) {
		var tableModel = (JsonTable<?>.JsonTableModel) table.get("tableModel");
		return (JsonComponent) ((IComponent[]) tableModel.get("headerFilters"))[column];
	}
	
	@Override
	public void setFilter(int column, String filterString) {
		HeadlessFormTestFacade.setText(getFilterComponent(column), filterString);
	}

	@Override
	public String getFilter(int column) {
		return HeadlessFormTestFacade.getText(getFilterComponent(column));
	}

	@Override
	public void filterLookup(int column) {
		var element = getFilterComponent(column);
		if (element instanceof JsonLookup jsonLookup) {
			jsonLookup.showLookupDialog();
		}
	}

	public static class HeadlessSearchTableTestFacade extends HeadlessTableTestFacade implements SearchTableTestFacade {

		public HeadlessSearchTableTestFacade(JsonTable<?> table) {
			super(table);
		}

		@Override
		public void search(String text) {
			// TODO
		}
	}
	
}

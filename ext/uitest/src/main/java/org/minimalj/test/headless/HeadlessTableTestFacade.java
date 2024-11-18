package org.minimalj.test.headless;

import java.util.List;
import java.util.Map;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.impl.json.JsonFormContent;
import org.minimalj.frontend.impl.json.JsonLookup;
import org.minimalj.frontend.impl.json.JsonTable;
import org.minimalj.frontend.impl.json.JsonTable.JsonTableModel;
import org.minimalj.test.PageContainerTestFacade.DialogTestFacade;
import org.minimalj.test.PageContainerTestFacade.FormTestFacade;
import org.minimalj.test.PageContainerTestFacade.SearchTableTestFacade;
import org.minimalj.test.PageContainerTestFacade.TableTestFacade;

public class HeadlessTableTestFacade implements TableTestFacade {
	final JsonTable<?> table;
	final JsonFormContent overview;

	public HeadlessTableTestFacade(JsonTable<?> table) {
		this(table, null);
	}
	
	public HeadlessTableTestFacade(JsonTable<?> table, JsonFormContent overview) {
		this.table = table;
		this.overview = overview;
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
			return (String)map.get("text");
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
		return new HeadlessFormTestFacade(overview);
	}
	
	@Override
	public void setFilterVisible(boolean visible) {
		table.setFilterVisible(visible);
	}

	@Override
	public void setFilter(int column, String filterString) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getFilter(int column) {
		var headerFilter = getHeaderFilters()[column];
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DialogTestFacade filterLookup(int column) {
		var headerFilter = getHeaderFilters()[column];
		if (headerFilter instanceof JsonLookup jsonLookup) {
			jsonLookup.showLookupDialog();
			
		}
		return null;
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

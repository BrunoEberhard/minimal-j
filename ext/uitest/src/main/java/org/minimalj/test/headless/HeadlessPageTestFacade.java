package org.minimalj.test.headless;

import org.minimalj.frontend.impl.json.JsonCustomFilter;
import org.minimalj.frontend.impl.json.JsonFormContent;
import org.minimalj.frontend.impl.json.JsonTable;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.test.PageContainerTestFacade.FormTestFacade;
import org.minimalj.test.PageContainerTestFacade.NavigationTestFacade;
import org.minimalj.test.PageContainerTestFacade.PageTestFacade;
import org.minimalj.test.PageContainerTestFacade.TableTestFacade;

public class HeadlessPageTestFacade implements PageTestFacade {
	private final Page page;
	private final HeadlessNavigationTestFacade contextMenu;
	
	public HeadlessPageTestFacade(Page page) {
		this.page = page;
		this.contextMenu = new HeadlessNavigationTestFacade(page.getActions());
	}

	@Override
	public String getTitle() {
		return page.getTitle();
	}

	@Override
	public NavigationTestFacade getContextMenu() {
		return contextMenu;
	}

	@Override
	public void executeQuery(String query) {
		// TODO Auto-generated method stub
	}

	@Override
	public TableTestFacade getTable() {
		if (page instanceof TablePage<?> tablePage) {
			var content = tablePage.getContent();
			if (content instanceof JsonCustomFilter jsonCustomFilter) {
				return new HeadlessTableTestFacade((JsonTable<?>) jsonCustomFilter.get("table"), (JsonFormContent) jsonCustomFilter.get("filter"));
			} else {
				return new HeadlessTableTestFacade((JsonTable<?>) tablePage.getContent());
			}
		}
		return null;
	}

	@Override
	public FormTestFacade getForm() {
		return new HeadlessFormTestFacade((JsonFormContent) page.getContent());
	}

	@Override
	public boolean contains(String string) {
		// TODO Auto-generated method stub
		return false;
	}

}

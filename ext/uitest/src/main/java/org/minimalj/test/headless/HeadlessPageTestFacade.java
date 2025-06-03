package org.minimalj.test.headless;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.impl.json.JsonCustomFilter;
import org.minimalj.frontend.impl.json.JsonFormContent;
import org.minimalj.frontend.impl.json.JsonHtmlContent;
import org.minimalj.frontend.impl.json.JsonTable;
import org.minimalj.frontend.page.Page;
import org.minimalj.test.PageContainerTestFacade.FormTestFacade;
import org.minimalj.test.PageContainerTestFacade.NavigationTestFacade;
import org.minimalj.test.PageContainerTestFacade.PageTestFacade;
import org.minimalj.test.PageContainerTestFacade.TableTestFacade;

public class HeadlessPageTestFacade implements PageTestFacade {
	private final Page page;
	private final HeadlessNavigationTestFacade contextMenu;
	private final IContent content;
	private final TableTestFacade table;
	private final FormTestFacade form;
	
	public HeadlessPageTestFacade(Page page) {
		this.page = page;
		this.contextMenu = new HeadlessNavigationTestFacade(page.getActions());
		content = page.getContent();
		if (content instanceof JsonCustomFilter jsonCustomFilter && jsonCustomFilter.get("table") instanceof JsonTable jsonTable) {
			table = new HeadlessTableTestFacade(jsonTable, (JsonFormContent) jsonCustomFilter.get("filter"));
			form = null;
		} else if (content instanceof JsonTable jsonTable){
			table = new HeadlessTableTestFacade(jsonTable);
			form = null;
		} else if (content instanceof JsonFormContent jsonFormContent) {
			table = null;
			form = new HeadlessFormTestFacade((JsonFormContent) page.getContent());
		} else {
			table = null;
			form = null;
		}
	}
	
	public Page getPage() {
		return page;
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
		return table;
	}

	@Override
	public FormTestFacade getForm() {
		return form;
	}

	@Override
	public boolean contains(String string) {
		if (content instanceof JsonHtmlContent htmlContent) {
			String html = (String) htmlContent.get("htmlOrUrl");
			return html.contains(string);
		} else {
			return false;
		}
	}

}

package org.minimalj.test.headless;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.json.JsonComponent;
import org.minimalj.frontend.impl.json.JsonFormContent;
import org.minimalj.frontend.impl.json.JsonTable;
import org.minimalj.frontend.page.Page.Dialog;
import org.minimalj.test.PageContainerTestFacade.ActionTestFacade;
import org.minimalj.test.PageContainerTestFacade.DialogTestFacade;
import org.minimalj.test.PageContainerTestFacade.FormTestFacade;
import org.minimalj.test.PageContainerTestFacade.SearchTableTestFacade;
import org.minimalj.test.PageContainerTestFacade.TableTestFacade;
import org.minimalj.test.headless.HeadlessTableTestFacade.HeadlessSearchTableTestFacade;

public class HeadlessDialogTestFacade implements DialogTestFacade {
	private final Dialog dialog;
	private final Map<String, HeadlessActionTestFacade> actions = new HashMap<>();

	public HeadlessDialogTestFacade(Dialog dialog) {
		this.dialog = dialog;
		if (dialog.getActions() != null) {
			dialog.getActions().forEach(this::addAction);
		}
		addAction(dialog.getSaveAction());
		addAction(dialog.getCancelAction());
	}
	
	public HeadlessDialogTestFacade(Dialog dialog, List<Action> actions) {
		this.dialog = dialog;
		actions.forEach(this::addAction);
	}

	@Override
	public String getTitle() {
		return dialog.getTitle();
	}
	
	public Dialog getDialog() {
		return dialog;
	}

	private void addAction(Action action) {
		if (action != null) {
			actions.put(action.getName(), new HeadlessActionTestFacade(action));
		}
	}

	@Override
	public void close() {
		((HeadlessFrontend) Frontend.getInstance()).closeDialog(dialog);
	}

	@Override
	public FormTestFacade getForm() {
		JsonComponent content = (JsonComponent) dialog.getContent();
		content = HeadlessFormTestFacade.unpackComponent(content);
		return new HeadlessFormTestFacade((JsonFormContent) content);
	}

	@Override
	public TableTestFacade getTable() {
		return new HeadlessTableTestFacade((JsonTable<?>) dialog.getContent());
	}

	@Override
	public SearchTableTestFacade getSearchTable() {
		return new HeadlessSearchTableTestFacade((JsonTable<?>) dialog.getContent());
	}

	@Override
	public ActionTestFacade getAction(String caption) {
		return actions.get(caption);
	}

}

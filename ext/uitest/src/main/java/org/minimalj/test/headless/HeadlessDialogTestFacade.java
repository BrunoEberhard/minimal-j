package org.minimalj.test.headless;

import java.util.HashMap;
import java.util.Map;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
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
		return new HeadlessFormTestFacade((JsonFormContent) dialog.getContent());
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

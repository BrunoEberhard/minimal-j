package org.minimalj.test.headless;

import java.util.Collection;
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

	@Override
	public FormTestFacade formWithElement(String caption) {
		JsonComponent content = (JsonComponent) dialog.getContent();
		content = HeadlessFormTestFacade.unpackComponent(content);
		JsonFormContent form = findForm(content, caption, null);
		return form != null ? new HeadlessFormTestFacade(form) : null;
	}

	@Override
	public FormTestFacade nextForm(FormTestFacade form) {
		if (!(form instanceof HeadlessFormTestFacade)) {
			return null;
		}
		JsonFormContent target = ((HeadlessFormTestFacade) form).getFormContent();
		JsonComponent content = (JsonComponent) dialog.getContent();
		content = HeadlessFormTestFacade.unpackComponent(content);
		JsonFormContent next = findNextForm(content, target);
		return next != null ? new HeadlessFormTestFacade(next) : null;
	}

	/**
	 * Searches for the parent form which contains {@code target} as a cell in one of
	 * its rows. If that row belongs to a group (and is not the last row of it) the
	 * form contained in the next row is returned, otherwise null.
	 */
	@SuppressWarnings("unchecked")
	private JsonFormContent findNextForm(Object o, JsonFormContent target) {
		if (o instanceof JsonFormContent parent) {
			List<List<JsonComponent>> rows = (List<List<JsonComponent>>) parent.get("rows");
			List<String> rowCss = (List<String>) parent.get("rowCss");
			for (int i = 0; i < rows.size(); i++) {
				for (JsonComponent cell : rows.get(i)) {
					if (HeadlessFormTestFacade.unpackComponent(cell) == target) {
						String css = rowCss.get(i);
						if (css == null || css.contains(JsonFormContent.GROUP_END) || css.contains(JsonFormContent.GROUP_SINGLE_ROW)
								|| i + 1 >= rows.size()) {
							return null; // last row of the group (or not grouped)
						}
						for (JsonComponent nextCell : rows.get(i + 1)) {
							JsonComponent next = HeadlessFormTestFacade.unpackComponent(nextCell);
							if (next instanceof JsonFormContent nextForm) {
								return nextForm;
							}
						}
						return null;
					}
				}
			}
		}
		if (o instanceof Collection<?> collection) {
			for (var item : collection) {
				var result = findNextForm(item, target);
				if (result != null) {
					return result;
				}
			}
		}
		if (o instanceof Map<?, ?> map) {
			for (var item : map.values()) {
				var result = findNextForm(item, target);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	/**
	 * Searches the component tree for an element with the given caption and returns
	 * the closest enclosing form.
	 */
	private JsonFormContent findForm(Object o, String caption, JsonFormContent closestForm) {
		if (o instanceof JsonComponent jsonComponent) {
			if (Boolean.TRUE.equals(jsonComponent.get("hideFormElement"))) {
				return null;
			}
			if (jsonComponent instanceof JsonFormContent jsonFormContent) {
				closestForm = jsonFormContent;
			}
			if (caption.equals(JsonFormContent.getCaptionOrName(jsonComponent))) {
				return closestForm;
			}
		}
		if (o instanceof Collection<?> collection) {
			for (var item : collection) {
				var result = findForm(item, caption, closestForm);
				if (result != null) {
					return result;
				}
			}
		}
		if (o instanceof Map<?, ?> map) {
			for (var item : map.values()) {
				var result = findForm(item, caption, closestForm);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

}

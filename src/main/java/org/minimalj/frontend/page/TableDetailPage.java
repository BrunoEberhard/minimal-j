package org.minimalj.frontend.page;

import java.util.Collections;
import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.TableActionListener;

public abstract class TableDetailPage<T> extends TablePage<T> implements TableActionListener<T> {

	private Page detailPage;

	public TableDetailPage() {
		super();
	}

	// better: createDetailPage
	protected abstract Page getDetailPage(T mainObject);

	protected Page getDetailPage(List<T> selectedObjects) {
		if (selectedObjects == null || selectedObjects.size() != 1) {
			return null;
		} else {
			return getDetailPage(selectedObjects.get(selectedObjects.size() - 1));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void action(T selectedObject) {
		if (detailPage instanceof ChangeableDetailPage) {
			((ChangeableDetailPage<T>) detailPage).setObjects(Collections.singletonList(selectedObject));
			setDetailPage(detailPage);
		} else {
			Page newDetailPage = getDetailPage(selectedObject);
			setDetailPage(newDetailPage);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void selectionChanged(List<T> selectedObjects) {
		super.selectionChanged(selectedObjects);
		boolean detailVisible = isDetailVisible();
		if (detailVisible && !selectedObjects.isEmpty()) {
			if (detailPage instanceof ChangeableDetailPage) {
				((ChangeableDetailPage<T>) detailPage).setObjects(selectedObjects);
			} else {
				Page newDetailPage = getDetailPage(selectedObjects);
				setDetailPage(newDetailPage);
			}
		}
	}

	private void setDetailPage(Page newDetailPage) {
		if (newDetailPage != null) {
			Frontend.showDetail(TableDetailPage.this, newDetailPage);
		} else if (detailPage != null) {
			Frontend.hideDetail(detailPage);
		}
		detailPage = newDetailPage;
	}

	protected boolean isDetailVisible() {
		return detailPage != null && Frontend.isDetailShown(detailPage);
	}

	public static interface ChangeableDetailPage<T> {
		public void setObjects(List<T> objects);
	}
}

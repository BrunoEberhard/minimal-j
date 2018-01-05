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
	
	public TableDetailPage(Object[] keys) {
		super(keys);
	}

	protected abstract Page getDetailPage(T mainObject);

	protected Page getDetailPage(List<T> selectedObjects) {
		if (selectedObjects == null || selectedObjects.size() != 1) {
			return null;
		} else {
			return getDetailPage(selectedObjects.get(0));
		}
	}
	
	@Override
	public void action(T selectedObject) {
		if (detailPage != null) {
			updateDetailPage(Collections.singletonList(selectedObject));
		} else {
			detailPage = getDetailPage(selectedObject);
			if (detailPage != null) {
				Frontend.showDetail(TableDetailPage.this, detailPage);
			}
		}
	}
	
	protected boolean isDetailVisible() {
		return detailPage != null && Frontend.isDetailShown(detailPage);
	}

	@Override
	public void selectionChanged(List<T> selectedObjects) {
		super.selectionChanged(selectedObjects);
		boolean detailVisible = detailPage != null && Frontend.isDetailShown(detailPage); 
		if (detailVisible) {
			if (selectedObjects != null && !selectedObjects.isEmpty()) {
				updateDetailPage(selectedObjects);
			} else {
				Frontend.hideDetail(detailPage);
			}
		}
	}
	
	private void updateDetailPage(List<T> selectedObjects) {
		Page updatedDetailPage = getDetailPage(selectedObjects);
		if (Frontend.isDetailShown(detailPage)) {
			if (updatedDetailPage == null || updatedDetailPage != detailPage) {
				Frontend.hideDetail(detailPage);
			}
		}
		if (updatedDetailPage != null) {
			Frontend.showDetail(TableDetailPage.this, updatedDetailPage);
			detailPage = updatedDetailPage;
		}
	}
	
}



package org.minimalj.frontend.page;

import java.util.Collections;
import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.TableActionListener;

public abstract class TableDetailPage<T, DETAIL_PAGE extends Page> extends TablePage<T> implements TableActionListener<T> {
	
	private DETAIL_PAGE detailPage;

	public TableDetailPage() {
		super();
	}
	
	public TableDetailPage(Object[] keys) {
		super(keys);
	}

	protected abstract DETAIL_PAGE createDetailPage(T mainObject);

	protected abstract DETAIL_PAGE updateDetailPage(DETAIL_PAGE page, T mainObject);

	protected DETAIL_PAGE updateDetailPage(DETAIL_PAGE page, List<T> selectedObjects) {
		if (selectedObjects == null || selectedObjects.size() != 1) {
			return null;
		} else {
			return updateDetailPage(page, selectedObjects.get(0));
		}
	}
	
	@Override
	public void action(T selectedObject) {
		if (detailPage != null) {
			updateDetailPage(Collections.singletonList(selectedObject));
		} else {
			detailPage = createDetailPage(selectedObject);
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
		DETAIL_PAGE updatedDetailPage = updateDetailPage(detailPage, selectedObjects);
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



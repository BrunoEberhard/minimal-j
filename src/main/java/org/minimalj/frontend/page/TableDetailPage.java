package org.minimalj.frontend.page;

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
		Page newDetailPage = getDetailPage(selectedObject);
		setDetailPage(newDetailPage);
	}

	@Override
	public void selectionChanged(List<T> selectedObjects) {
		super.selectionChanged(selectedObjects);
		boolean detailVisible = isDetailVisible(); 
		if (detailVisible && !selectedObjects.isEmpty()) {
			Page newDetailPage = getDetailPage(selectedObjects.get(0));
			setDetailPage(newDetailPage);
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

}

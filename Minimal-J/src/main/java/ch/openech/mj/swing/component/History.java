package ch.openech.mj.swing.component;

import java.util.ArrayList;
import java.util.List;

public class History<T> {

	private final List<T> history = new ArrayList<T>();
	private final HistoryListener historyListener;
	private T present;
	
	public History() {
		this(null);
	}
	
	public History(HistoryListener historyListener) {
		this.historyListener = historyListener;
	}
	
	public void add(T page) {
		if (page.equals(present)) return;
		int indexToRemove = history.size() - 1;
		while (indexToRemove > getVisibleIndex()) {
			history.remove(indexToRemove--);
		}
		
		present = page;
		history.add(page);
		
		fireHistoryChanged();
	}

	private void fireHistoryChanged() {
		if (historyListener != null) {
			historyListener.onHistoryChanged();
		}
	}
	
	public void replace(T page) {
		if (page.equals(present)) return;
		
		history.set(getVisibleIndex(), page);
		present = page;
		
		fireHistoryChanged();
	}
	
	private void moveTo(T page) {
		present = page;
		
		fireHistoryChanged();
	}
	
	public void next() {
		int visibleIndex = getVisibleIndex();
		moveTo(history.get(visibleIndex + 1));
	}

	public void previous() {
		int visibleIndex = getVisibleIndex();
		moveTo(history.get(visibleIndex - 1));
	}
	
	public void dropFuture() {
		int visibleIndex = getVisibleIndex();
		for (int i = history.size() - 1; i > visibleIndex; i--) {
			history.remove(i);
		}
		fireHistoryChanged();
	}
	
	public T getPresent() {
		return present;
	}
	
	private int getVisibleIndex() {
		return history.indexOf(present);
	}

	public boolean hasFuture() {
		return present != null && history.indexOf(present) < history.size() - 1; 
	}

	public boolean hasPast() {
		return present != null && history.indexOf(present) > 0; 
	}
	
	public interface HistoryListener {
		public void onHistoryChanged();
	}
}

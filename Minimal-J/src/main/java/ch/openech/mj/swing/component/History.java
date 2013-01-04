package ch.openech.mj.swing.component;

import java.util.ArrayList;
import java.util.List;

public class History<T> {

	private final List<T> history = new ArrayList<T>();
	private final HistoryListener historyListener;
	private T present;
	private int presentIndex = -1;
	
	public History() {
		this(null);
	}
	
	public History(HistoryListener historyListener) {
		this.historyListener = historyListener;
	}
	
	public void add(T page) {
		if (!page.equals(present)) {
			int indexToRemove = history.size() - 1;
			while (indexToRemove > presentIndex) {
				history.remove(indexToRemove--);
			}
			
			present = page;
			history.add(page);
			presentIndex = history.size() - 1;
		}
		fireHistoryChanged();
	}

	private void fireHistoryChanged() {
		if (historyListener != null) {
			historyListener.onHistoryChanged();
		}
	}
	
	public void replace(T page) {
		if (page.equals(present)) return;
		
		history.set(presentIndex, page);
		present = page;
		
		fireHistoryChanged();
	}
	
	private void moveTo(T page) {
		present = page;
		
		fireHistoryChanged();
	}
	
	public void next() {
		moveTo(history.get(++presentIndex));
	}

	public void previous() {
		moveTo(history.get(--presentIndex));
	}
	
	public void dropFuture() {
		for (int i = history.size() - 1; i > presentIndex; i--) {
			history.remove(i);
		}
		fireHistoryChanged();
	}
	
	public T getPresent() {
		return present;
	}
	
	public boolean hasFuture() {
		return present != null && presentIndex < history.size() - 1; 
	}

	public boolean hasPast() {
		return present != null && presentIndex > 0; 
	}
	
	public interface HistoryListener {
		public void onHistoryChanged();
	}
}

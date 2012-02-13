package ch.openech.mj.swing.component;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

public class UnselectableSelectionListModel implements ListSelectionModel {

	@Override
	public void addListSelectionListener(ListSelectionListener x) {
		// do nothing
	}

	@Override
	public void addSelectionInterval(int index0, int index1) {
		// do nothing
	}

	@Override
	public void clearSelection() {
		// do nothing
	}

	@Override
	public int getAnchorSelectionIndex() {
		return -1;
	}

	@Override
	public int getLeadSelectionIndex() {
		return -1;
	}

	@Override
	public int getMaxSelectionIndex() {
		return -1;
	}

	@Override
	public int getMinSelectionIndex() {
		return -1;
	}

	@Override
	public int getSelectionMode() {
		return 0;
	}

	@Override
	public boolean getValueIsAdjusting() {
		return false;
	}

	@Override
	public void insertIndexInterval(int index, int length, boolean before) {
		// do nothing
	}

	@Override
	public boolean isSelectedIndex(int index) {
		return false;
	}

	@Override
	public boolean isSelectionEmpty() {
		return true;
	}

	@Override
	public void removeIndexInterval(int index0, int index1) {
		// do nothing
	}

	@Override
	public void removeListSelectionListener(ListSelectionListener x) {
		// do nothing
	}

	@Override
	public void removeSelectionInterval(int index0, int index1) {
		// do nothing
	}

	@Override
	public void setAnchorSelectionIndex(int index) {
		// do nothing
	}

	@Override
	public void setLeadSelectionIndex(int index) {
		// do nothing
	}

	@Override
	public void setSelectionInterval(int index0, int index1) {
		// do nothing
	}

	@Override
	public void setSelectionMode(int selectionMode) {
		// do nothing
	}

	@Override
	public void setValueIsAdjusting(boolean valueIsAdjusting) {
		// do nothing
	}

}

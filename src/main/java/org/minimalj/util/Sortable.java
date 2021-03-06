package org.minimalj.util;

public interface Sortable {

	public void sort(Object[] sortKeys, boolean[] sortDirections);

	public boolean canSortBy(Object sortKey);
}

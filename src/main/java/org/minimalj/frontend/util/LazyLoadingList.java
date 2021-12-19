package org.minimalj.frontend.util;

import java.util.AbstractList;
import java.util.List;

import org.minimalj.frontend.impl.util.ColumnFilter;

public abstract class LazyLoadingList<T> extends AbstractList<T> {

	@Override
	public T get(int index) {
		throw new RuntimeException("Should not be called");
	}
	
	public abstract List<T> get(ColumnFilter[] filters, Object[] sortKeys, boolean[] sortDirections, int page, int pageSize);

	public abstract int count(ColumnFilter[] filters);

}
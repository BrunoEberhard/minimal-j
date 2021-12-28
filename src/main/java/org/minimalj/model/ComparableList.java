package org.minimalj.model;

import java.util.ArrayList;
import java.util.List;

// TODO is this possible without warnings?
public class ComparableList<E, COMPARABLE extends Comparable> extends ArrayList<E> implements Comparable<ComparableList<E, Comparable>>{

	private final COMPARABLE comparable;
	
	public ComparableList() {
		// for Keys
		this.comparable = null;
	}
	
	public ComparableList(List<E> list, COMPARABLE comparable) {
		super(list);
		this.comparable = comparable;
	}

	@Override
	public int compareTo(ComparableList<E, Comparable> o) {
		return comparable.compareTo(o.comparable);
	}
	
	public COMPARABLE getComparable() {
		return comparable;
	}
	
}

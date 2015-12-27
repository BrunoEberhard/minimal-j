package org.minimalj.model;

import java.util.AbstractList;

public class UnloadedList<E> extends AbstractList<E> {

	private final Class<E> elementClass;
	
	public UnloadedList(Class<E> elementClass) {
		this.elementClass = elementClass;
	}
	
	@Override
	public E get(int index) {
		throw new RuntimeException(getExceptionMessage());
	}

	@Override
	public int size() {
		throw new RuntimeException(getExceptionMessage());
	}

	@Override
    public E set(int index, E element) {
		throw new RuntimeException(getExceptionMessage());
    }

	@Override
    public void add(int index, E element) {
		throw new RuntimeException(getExceptionMessage());
    }

	@Override
    public E remove(int index) {
		throw new RuntimeException(getExceptionMessage());
    }
	
	private String getExceptionMessage() {
		return "Access on unloaded list of " + elementClass.getSimpleName();
	}
}

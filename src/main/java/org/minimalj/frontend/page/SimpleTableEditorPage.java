package org.minimalj.frontend.page;

import org.minimalj.util.CloneHelper;

public abstract class SimpleTableEditorPage<T> extends BaseTableEditorPage<T, T> {

	public SimpleTableEditorPage() {
		super();
	}

	public SimpleTableEditorPage(Object[] columns) {
		super(columns);
	}
	
	@Override
	protected T createObject() {
		return CloneHelper.newInstance(clazz.getClazz());
	}

	@Override
	protected T viewed(T object) {
		return object;
	}
	
	@Override
	protected T view(T object) {
		return object;
	}
}

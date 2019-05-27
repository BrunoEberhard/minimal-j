package org.minimalj.frontend.page;

import org.minimalj.util.CloneHelper;

public abstract class SimpleTableEditorPage<T> extends BaseTableEditorPage<T, T> {

	public SimpleTableEditorPage() {
		super();
	}

	@Override
	protected T createObject() {
		return CloneHelper.newInstance(getClazz());
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

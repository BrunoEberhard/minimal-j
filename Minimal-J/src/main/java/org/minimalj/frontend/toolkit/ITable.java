package org.minimalj.frontend.toolkit;

import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit.IContent;


public interface ITable<T> extends IContent {

	public void setObjects(List<T> objects);

	public void setClickListener(TableActionListener<T> listener);
	public void setDeleteListener(TableActionListener<T> listener);
	public void setInsertListener(InsertListener listener);
	public void setFunctionListener(int function, TableActionListener<T> listener);
	
	public static interface TableActionListener<U> {
		public void action(U selectedObject, List<U> selectedObjects);
	}
	
	public static interface InsertListener {
		public void action();
	}

}

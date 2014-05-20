package org.minimalj.frontend.toolkit;

import java.util.List;


public interface ITable<T> extends IComponent {

	public void setObjects(List<T> objects);

	// TODO
	// public void setObjects(List<?> ids, Lookup<T> lookup);
	// 
	// public static interface Lookup<U> {
	//	public U lookup(long id);
	// }
	
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

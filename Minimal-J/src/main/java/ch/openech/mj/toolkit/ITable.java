package ch.openech.mj.toolkit;

import java.util.List;


public interface ITable<T> extends IComponent {

	public void setIds(List<Integer> object);

	public void setClickListener(TableActionListener<T> listener);
	public void setDeleteListener(TableActionListener<T> listener);
	public void setInsertListener(InsertListener listener);
	public void setFunctionListener(int function, TableActionListener<T> listener);
	
	public static interface TableActionListener<S> {
		public void action(S selectedObject, List<S> selectedObjects);
	}
	
	public static interface InsertListener {
		public void action();
	}

}

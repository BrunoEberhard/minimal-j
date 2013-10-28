package ch.openech.mj.toolkit;

import java.util.List;


public interface ITable<T> extends IComponent {

	public void setIds(List<Integer> object);

	public void setClickListener(TableActionListener listener);
	public void setDeleteListener(TableActionListener listener);
	public void setInsertListener(InsertListener listener);
	public void setFunctionListener(int function, TableActionListener listener);
	
	public static interface TableActionListener {
		public void action(int selectedId, List<Integer> selectedIds);
	}
	
	public static interface InsertListener {
		public void action();
	}

}

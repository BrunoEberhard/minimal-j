package ch.openech.mj.toolkit;

import java.util.List;

import ch.openech.mj.search.Item;


public interface ITable extends IComponent {

	public void setObjects(List<? extends Item> object);

	public void setClickListener(TableActionListener listener);
	public void setDeleteListener(TableActionListener listener);
	public void setInsertListener(InsertListener listener);
	public void setFunctionListener(int function, TableActionListener listener);
	
	public static interface TableActionListener {
		public void action(Item selectedObject, List<Item> selectedObjects);
	}
	
	public static interface InsertListener {
		public void action();
	}

}

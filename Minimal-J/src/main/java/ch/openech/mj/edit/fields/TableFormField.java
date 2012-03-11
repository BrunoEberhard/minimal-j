package ch.openech.mj.edit.fields;

import java.util.List;

import ch.openech.mj.db.model.Constants;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponentDelegate;
import ch.openech.mj.toolkit.VisualTable;

public class TableFormField implements IComponentDelegate, ch.openech.mj.edit.fields.FormField<List> {

	private final String name;
	private final VisualTable<?> table;
	
	public TableFormField(Object key, Class<?> clazz, Object[] fields) {
		this.name = Constants.getConstant(key);
		
		table = ClientToolkit.getToolkit().createVisualTable(clazz, fields);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getComponent() {
		return table;
	}

	@Override
	public void setObject(List objects) {
		table.setObjects(objects);
	}

}

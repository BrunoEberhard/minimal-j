package ch.openech.mj.vaadin.toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.ReadablePartial;

import ch.openech.mj.model.Keys;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.VisualTable;
import ch.openech.mj.util.JodaFormatter;
import ch.openech.mj.vaadin.PropertyVaadinContainer;

import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Table;

public class VaadinVisualTable<T> extends Table implements VisualTable<T> {

	private final Class<T> clazz;
	private final List<PropertyInterface> properties = new ArrayList<PropertyInterface>();
	private final JodaFormatter jodaFormatter = new JodaFormatter();
	private List<T> objects;
	private ActionListener listener;
	private VaadinVisualTableItemClickListener tableClickListener;

	public VaadinVisualTable(Class<T> clazz, Object[] keys) {
		this.clazz = clazz;
		setSelectable(true);
		setMultiSelect(false);
		setSizeFull();
		
		for (Object key : keys) {
			PropertyInterface property = Keys.getProperty(key);
			properties.add(property);
			String header = Resources.getObjectFieldName(Resources.getResourceBundle(), property);
			setColumnHeader(property, header);
		}
	}
	
	@Override
	public void requestFocus() {
		focus();
	}

	@Override
	public void setObjects(List<T> list) {
		this.objects = list;
		setContainerDataSource(new PropertyVaadinContainer(clazz, list, properties));
	}

	@Override
	public T getSelectedObject() {
		if (getValue() != null) {
			return objects.get((Integer)getValue());
		} else {
			return null;
		}
	}

	@Override
	public void setClickListener(ActionListener clickListener) {
		if (clickListener == null) {
			if (tableClickListener != null) {
				removeListener(tableClickListener);
				tableClickListener = null;
			}
		}
		this.listener = clickListener;
		if (clickListener != null) {
			if (tableClickListener == null) {
				tableClickListener = new VaadinVisualTableItemClickListener();
				addListener(tableClickListener);
			}
		}
	}
	
	@Override
	protected String formatPropertyValue(Object rowId, Object colId,
			Property property) {
		Object v = property.getValue();
		if (v instanceof ReadablePartial) {
			return jodaFormatter.format(v, (PropertyInterface) colId);
		}
		return super.formatPropertyValue(rowId, colId, property);
	}
     
	private class VaadinVisualTableItemClickListener implements ItemClickListener {
		@Override
		public void itemClick(ItemClickEvent arg0) {
			ActionEvent actionEvent = new ActionEvent(VaadinVisualTable.this, 0, "Insert");
			listener.actionPerformed(actionEvent);
		}
	}

	// TODO !
	
	@Override
	public List<T> getSelectedObjects() {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	@Override
	public void setDeleteListener(ActionListener listener) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setInsertListener(ActionListener listener) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setFunctionListener(int function, ActionListener listener) {
		// TODO Auto-generated method stub
	}
}

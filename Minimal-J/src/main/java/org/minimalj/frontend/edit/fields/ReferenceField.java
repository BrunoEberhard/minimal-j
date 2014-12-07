package org.minimalj.frontend.edit.fields;

import java.util.List;
import java.util.Locale;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.ClientToolkit.ILookup;
import org.minimalj.frontend.toolkit.ClientToolkit.InputComponentListener;
import org.minimalj.frontend.toolkit.ClientToolkit.Search;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.ViewUtil;
import org.minimalj.transaction.criteria.Criteria;
import org.minimalj.util.CloneHelper;

public class ReferenceField<T> extends AbstractEditField<T> {
	// private static final Logger logger = Logger.getLogger(ReferenceField.class.getName());
	
	private final PropertyInterface property;
	private final Class<?> fieldClazz;
	private final Object[] searchColumns;
	protected final ILookup<T> lookup;
	private T object;
	
	public ReferenceField(Object key, Object... searchColumns) {
		this(key, searchColumns, true);
	}

	public ReferenceField(Object key, Object[] searchColumns, boolean editable) {
		super(key, editable);
		property = Keys.getProperty(key);
		fieldClazz = ViewUtil.resolve(property.getFieldClazz());
		this.searchColumns = searchColumns;
		lookup = ClientToolkit.getToolkit().createLookup(new ReferenceFieldChangeListener(), new ReferenceFieldSearch(), searchColumns);
	}

	private class ReferenceFieldSearch implements Search<T> {

		@Override
		public List<T> search(String searchText) {
			return (List<T>) Backend.getInstance().read(fieldClazz, Criteria.search(searchText, searchColumns), 100);
		}
	}
	
	@Override
	public IComponent getComponent() {
		return lookup;
	}

	@Override
	public T getObject() {
		return object;
	}

	@Override
	public void setObject(T object) {
		this.object = object;
		fireObjectChange();
	}
	
	protected void fireObjectChange() {
		display();
		super.fireChange();
	}

	protected void display() {
		if (object instanceof Rendering) {
			Rendering rendering = (Rendering) object;
			lookup.setText(rendering.render(RenderType.PLAIN_TEXT, Locale.getDefault()));
		} else if (object != null) {
			lookup.setText(object.toString());
		} else {
			lookup.setText(null);
		}
	}

	private class ReferenceFieldChangeListener implements InputComponentListener {

		@Override
		public void changed(IComponent source) {
			Object selectedObject = lookup.getSelectedObject();
			@SuppressWarnings("unchecked")
			T objectAsView = (T) ViewUtil.view(selectedObject, CloneHelper.newInstance(property.getFieldClazz()));
			setObject(objectAsView);
		}
		
	}

}

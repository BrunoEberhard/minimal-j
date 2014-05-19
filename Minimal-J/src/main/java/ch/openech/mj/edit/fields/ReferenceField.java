package ch.openech.mj.edit.fields;

import java.util.List;

import ch.openech.mj.backend.Backend;
import ch.openech.mj.edit.value.CloneHelper;
import ch.openech.mj.model.Keys;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.model.ViewUtil;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ClientToolkit.ILookup;
import ch.openech.mj.toolkit.ClientToolkit.InputComponentListener;
import ch.openech.mj.toolkit.ClientToolkit.Search;
import ch.openech.mj.toolkit.IComponent;

public class ReferenceField<T> extends AbstractEditField<T> {
	// private static final Logger logger = Logger.getLogger(ReferenceField.class.getName());
	
	private final PropertyInterface property;
	private final Object[] searchColumns;
	protected final ILookup<T> lookup;
	private T object;
	
	public ReferenceField(Object key, Object... searchColumns) {
		this(key, searchColumns, true);
	}

	public ReferenceField(Object key, Object[] searchColumns, boolean editable) {
		super(key, editable);
		property = Keys.getProperty(key);
		this.searchColumns = searchColumns;
		lookup = ClientToolkit.getToolkit().createLookup(new ReferenceFieldChangeListener(), new ReferenceFieldSearch(), searchColumns);
	}

	private class ReferenceFieldSearch implements Search<T> {

		@Override
		public List<T> search(String query) {
			return (List<T>) Backend.getInstance().search(property.getFieldClazz(), searchColumns, query, 100);
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
		if (object != null) {
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

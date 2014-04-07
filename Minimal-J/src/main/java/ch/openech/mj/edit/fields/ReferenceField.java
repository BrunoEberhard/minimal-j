package ch.openech.mj.edit.fields;

import ch.openech.mj.model.Keys;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.model.Search;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ClientToolkit.ILookup;
import ch.openech.mj.toolkit.ClientToolkit.InputComponentListener;
import ch.openech.mj.toolkit.IComponent;

public class ReferenceField<T> extends AbstractEditField<T> {
	// private static final Logger logger = Logger.getLogger(ReferenceField.class.getName());
	
	private T object;
	protected final ILookup<T> lookup;
	private final Object[] searchColumns;

	public ReferenceField(Object key, Search<T> search) {
		this(key, search, search.getKeys());
	}
	
	public ReferenceField(Object key, Search<T> search, Object... searchColumns) {
		this(key, search, searchColumns, true);
	}

	public ReferenceField(Object key, Search<T> search, Object[] searchColumns, boolean editable) {
		super(key, editable);
		PropertyInterface property = Keys.getProperty(key);
		lookup = ClientToolkit.getToolkit().createLookup(new ReferenceFieldChangeListener(), search, searchColumns);
		this.searchColumns = searchColumns;
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
			setObject(lookup.getSelectedObject());
		}
		
	}

}

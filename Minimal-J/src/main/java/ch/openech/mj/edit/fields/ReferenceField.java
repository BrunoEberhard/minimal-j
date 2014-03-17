package ch.openech.mj.edit.fields;

import ch.openech.mj.model.Keys;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.model.Reference;
import ch.openech.mj.model.Search;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ClientToolkit.ILookup;
import ch.openech.mj.toolkit.ClientToolkit.InputComponentListener;
import ch.openech.mj.toolkit.IComponent;

public class ReferenceField<T> extends AbstractEditField<Reference<T>> {
	// private static final Logger logger = Logger.getLogger(ReferenceField.class.getName());
	
	private Reference<T> reference;
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
		if (property.getFieldClazz() != Reference.class) throw new IllegalArgumentException("ReferenceField can only used for Fields of Class Reference. Wrong key: " + property.getFieldPath());
		lookup = ClientToolkit.getToolkit().createLookup(new ReferenceFieldChangeListener(), search, searchColumns);
		this.searchColumns = searchColumns;
	}

	@Override
	public IComponent getComponent() {
		return lookup;
	}

	@Override
	public Reference<T> getObject() {
		return reference;
	}

	@Override
	public void setObject(Reference<T> reference) {
		this.reference = reference;
		fireObjectChange();
	}
	
	protected void fireObjectChange() {
		display();
		super.fireChange();
	}

	protected void display() {
		if (!reference.isEmpty()) {
			String s = displayText();
			lookup.setText(s);
		} else {
			lookup.setText(null);
		}
	}

	protected String displayText() {
		StringBuilder s = new StringBuilder();
		for (Object key : searchColumns) {
			try {
				s.append(reference.get(key));
			} catch (IllegalArgumentException x) {
				String fieldPath = Keys.getProperty(key).getFieldPath();
				s.append("!" + fieldPath + "!");
			}
			s.append(' ');
		}
		return s.toString().trim();
	}
	
	private class ReferenceFieldChangeListener implements InputComponentListener {

		@Override
		public void changed(IComponent source) {
			reference.set(lookup.getSelectedObject());
			fireObjectChange();
		}
		
	}

}

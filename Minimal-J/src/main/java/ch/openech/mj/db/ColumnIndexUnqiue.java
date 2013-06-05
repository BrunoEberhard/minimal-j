package ch.openech.mj.db;//

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import ch.openech.mj.model.Keys;
import ch.openech.mj.model.PropertyInterface;

public class ColumnIndexUnqiue<T> implements Index<T> {
	private static final Logger logger = Logger.getLogger(ColumnIndexUnqiue.class.getName());
	
	private final Table<T> table;
	private final PropertyInterface property;
	private final Map<Object, T> index = new HashMap<>();
	
	public ColumnIndexUnqiue(Table<T> table, Object key) {
		this.table = table;
		this.property = Keys.getProperty(key);
	}
	
	@Override
	public void insert(int id, T object) {
		Object key = property.getValue(object);
		index.put(key, object);
	}

	@Override
	public void update(int id, T object) {
		insert(id, object);
	}

	@Override
	public void clear() {
		index.clear();
	}

	public T find(Object key) {
		return index.get(key);
	}
}

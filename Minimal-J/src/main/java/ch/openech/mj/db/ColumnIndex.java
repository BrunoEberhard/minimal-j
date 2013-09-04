package ch.openech.mj.db;//

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.openech.mj.model.Keys;
import ch.openech.mj.model.PropertyInterface;

public class ColumnIndex<T> implements Index<T> {
	private final PropertyInterface property;
	private final Map<Object, List<T>> index = new HashMap<>();
	private final Map<T, Object> revertIndex = new HashMap<>();
	
	public ColumnIndex(Table<T> table, Object key) {
		this.property = Keys.getProperty(key);
	}
	
	@Override
	public void insert(int id, T object) {
		Object key = property.getValue(object);
		if (!index.containsKey(key)) {
			List<T> list = new ArrayList<>();
			index.put(key, list);
		}
		List<T> list = index.get(key);
		list.add(object);
		
		revertIndex.put(object, key);
	}

	@Override
	public void update(int id, T object) {
		Object oldKey = revertIndex.get(object);
		index.get(oldKey).remove(object);
		insert(id, object);
	}

	@Override
	public void clear() {
		index.clear();
		revertIndex.clear();
	}

	public int count(Object key) {
		if (index.containsKey(key)) {
			return index.get(key).size();
		} else {
			return 0;
		}
	}
	
	public List<T> find(Object key) {
		return index.get(key);
	}
}

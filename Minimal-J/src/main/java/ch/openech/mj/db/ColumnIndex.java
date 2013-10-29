package ch.openech.mj.db;//

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.openech.mj.model.Keys;
import ch.openech.mj.model.PropertyInterface;

public class ColumnIndex<T> implements Index<T> {
	private final Table<T> table;
	private final PropertyInterface property;
	private final Map<Object, List<Integer>> index = new HashMap<>();
	private final Map<Integer, Object> revertIndex = new HashMap<>();
	
	public ColumnIndex(Table<T> table, Object key) {
		this.table = table;
		this.property = Keys.getProperty(key);
	}
	
	@Override
	public void insert(int id, T object) {
		Object key = property.getValue(object);
		if (!index.containsKey(key)) {
			List<Integer> list = new ArrayList<>();
			index.put(key, list);
		}
		List<Integer> list = index.get(key);
		list.add(id);
		
		revertIndex.put(id, key);
	}

	@Override
	public void update(int id, T object) {
		Object oldKey = revertIndex.get(id);
		index.get(oldKey).remove(id);
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
	
	public List<Integer> findId(Object key) {
		return index.get(key);
	}
	
	public List<T> find(Object key) {
		List<Integer> ids = findId(key);
		List<T> result = new ArrayList<>(ids.size());
		for (Integer id : ids) {
			result.add(table.read(id));
		}
		return result;
	}

}

package ch.openech.mj.db;

import java.util.ArrayList;
import java.util.List;

public class MultiIndex<T> implements Index<T> {

	private final ColumnIndex<T>[] indexes;
	
	public MultiIndex(ColumnIndex<T>[] indexes) {
		this.indexes = indexes;
	}

	public List<T> findObjects(Object query) {
		List<T> result = new ArrayList<>(50);
		for (ColumnIndex<T> index : indexes) {
			List<T> objects = index.findObjects(query);
			for (T object : objects) {
				if (!result.contains(object)) {
					result.add(object);
				}
			}
		}
		return result;
	}
	
	public List<Integer> findIds(Object query) {
		List<Integer> result = new ArrayList<>(50);
		for (ColumnIndex<T> index : indexes) {
			List<Integer> ids = index.findIds(query);
			for (Integer id : ids) {
				if (!result.contains(id)) {
					result.add(id);
				}
			}
		}
		return result;
	}
	
	@Override
	public T lookup(Integer id) {
		return indexes[0].lookup(id);
	}
	
	@Override
	public String getColumn() {
		return null;
	}

}

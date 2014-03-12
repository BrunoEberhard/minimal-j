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
	
	public List<Long> findIds(Object query) {
		List<Long> result = new ArrayList<>(50);
		for (ColumnIndex<T> index : indexes) {
			List<Long> ids = index.findIds(query);
			for (Long id : ids) {
				if (!result.contains(id)) {
					result.add(id);
				}
			}
		}
		return result;
	}
	
	@Override
	public T lookup(Long id) {
		return indexes[0].lookup(id);
	}
	
	@Override
	public String getColumn() {
		return null;
	}

}

package ch.openech.mj.search;

import java.util.List;
import java.util.WeakHashMap;

import ch.openech.mj.db.Index;

public class IndexSearch<T> implements Search<T> {

	private final Index<T> index;
	private final WeakHashMap<Integer, T> cache = new WeakHashMap<>();
	
	public IndexSearch(Index<T> index) {
		this.index = index;
	}
	
	@Override
	public List<Integer> search(String text) {
		return index.findIds(text);
	}

	@Override
	public T lookup(int id) {
		if (cache.containsKey(id)) {
			return cache.get(id);
		} else {
			T object = index.lookup(id);
			cache.put(id, object);
			return object;
		}
	}
	
}

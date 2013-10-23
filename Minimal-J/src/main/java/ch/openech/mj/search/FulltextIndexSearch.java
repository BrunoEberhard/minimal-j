package ch.openech.mj.search;

import java.util.List;

import ch.openech.mj.db.FulltextIndex;

public class FulltextIndexSearch<T> implements Search<T> {

	private final Class<T> clazz;
	private final FulltextIndex<T> index;
	private final Object[] keys;

	public FulltextIndexSearch(Class<T> clazz, FulltextIndex<T> index, Object... keys) {
		if (keys == null) throw new IllegalArgumentException();
		
		this.index = index;
		this.clazz = clazz;
		this.keys = keys;
	}
	
	@Override
	public Class<T> getClazz() {
		return clazz;
	}

	@Override
	public List<Item> search(String text) {
		return index.findItems(text);
	}

	@Override
	public Object[] getKeys() {
		return keys;
	}

	@Override
	public T lookup(Item item) {
		return index.lookup(item);
	}
	
}

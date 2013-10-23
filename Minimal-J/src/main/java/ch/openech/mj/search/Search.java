package ch.openech.mj.search;

import java.util.List;

public interface Search<T> {

	public Class<T> getClazz();
	
	public List<Item> search(String text);
	
	public T lookup(Item item);
	
	public Object[] getKeys();
	
}

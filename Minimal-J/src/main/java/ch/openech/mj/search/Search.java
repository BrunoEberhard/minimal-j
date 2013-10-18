package ch.openech.mj.search;

import java.util.List;

public interface Search<T> {

	public Class<T> getClazz();
	
	public List<T> search(String text);
	
	public Object[] getKeys();
	
}

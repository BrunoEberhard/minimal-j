package ch.openech.mj.search;

import java.util.List;

public interface Search<T> extends Lookup<T> {

	public List<Integer> search(String text);
	
}

package ch.openech.mj.db;

import java.util.List;

public interface Index<T> {

	public String getColumn();

	public List<Integer> findIds(Object query);

	public T lookup(Integer id);

}
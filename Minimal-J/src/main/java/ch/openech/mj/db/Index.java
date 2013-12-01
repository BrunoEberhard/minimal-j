package ch.openech.mj.db;

import java.sql.Connection;
import java.util.List;

public interface Index<T> {

	public String getColumn();

	public List<Integer> findIds(Connection connection, Object query);

	public T lookup(Connection connection, Integer id);

}
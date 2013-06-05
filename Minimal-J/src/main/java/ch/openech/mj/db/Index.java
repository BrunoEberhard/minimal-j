package ch.openech.mj.db;


public interface Index<T> {

	public void insert(int id, T object);

	public void update(int id, T object);

	public void clear();

}
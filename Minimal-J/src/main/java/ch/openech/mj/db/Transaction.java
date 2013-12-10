package ch.openech.mj.db;


public interface Transaction<T> {
	
    public T execute();

}

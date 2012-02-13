package ch.openech.mj.db;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

@SuppressWarnings({"rawtypes", "unchecked"})
public class DbList implements List {

	private List list;
	
	private final SubTable subTable;
	private final int id;
	private final Integer time;
	
	public DbList(SubTable subTable, int id, Integer time) {
		this.subTable = subTable;
		this.id = id;
		this.time = time;
	}
	
	private List loadList() {
		try {
			return subTable.read(id, time);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private List getList() {
		if (!isLoaded()) {
			list = loadList();
		}
		return list;
	}
	
	public boolean isLoaded() {
		return list != null;
	}

	// Delegation
	
	@Override
	public int size() {
		return getList().size();
	}

	@Override
	public boolean isEmpty() {
		return getList().isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return getList().contains(o);
	}

	@Override
	public Iterator iterator() {
		return getList().iterator();
	}

	@Override
	public Object[] toArray() {
		return getList().toArray();
	}

	@Override
	public Object[] toArray(Object[] a) {
		return getList().toArray(a);
	}

	@Override
	public boolean add(Object e) {
		return getList().add(e);
	}

	@Override
	public boolean remove(Object o) {
		return getList().remove(o);
	}

	@Override
	public boolean containsAll(Collection c) {
		return getList().containsAll(c);
	}

	@Override
	public boolean addAll(Collection c) {
		return getList().addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection c) {
		return getList().addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection c) {
		return getList().removeAll(c);
	}

	@Override
	public boolean retainAll(Collection c) {
		return getList().retainAll(c);
	}

	@Override
	public void clear() {
		getList().clear();
	}

	@Override
	public boolean equals(Object o) {
		return getList().equals(o);
	}

	@Override
	public int hashCode() {
		return getList().hashCode();
	}

	@Override
	public Object get(int index) {
		return getList().get(index);
	}

	@Override
	public Object set(int index, Object element) {
		return getList().set(index, element);
	}

	@Override
	public void add(int index, Object element) {
		getList().add(index, element);
	}

	@Override
	public Object remove(int index) {
		return getList().remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return getList().indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return getList().lastIndexOf(o);
	}

	@Override
	public ListIterator listIterator() {
		return getList().listIterator();
	}

	@Override
	public ListIterator listIterator(int index) {
		return getList().listIterator(index);
	}

	@Override
	public List subList(int fromIndex, int toIndex) {
		return getList().subList(fromIndex, toIndex);
	}

}

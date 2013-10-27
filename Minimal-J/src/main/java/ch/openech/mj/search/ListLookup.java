package ch.openech.mj.search;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ListLookup<T> implements Lookup<T> {

	private List<T> list;

	public ListLookup() {
	}

	public void setList(List<T> list) {
		this.list = list;
	}
	
	@Override
	public T lookup(int id) {
		return list.get(id);
	}
	
	public List<Integer> getIds() {
		return new IndexList(list.size());
	}
	
	public static class IndexList implements List<Integer> {
		private final int size;
		
		public IndexList(int size) {
			this.size = size;
		}
		
		@Override
		public int size() {
			return size;
		}

		@Override
		public boolean isEmpty() {
			return size == 0;
		}

		@Override
		public boolean contains(Object o) {
			if (o instanceof Integer) {
				int i = (Integer) o;
				return i < size;
			} else {
				return false;
			}
		}

		@Override
		public Iterator<Integer> iterator() {
			throw new RuntimeException();
		}

		@Override
		public Object[] toArray() {
			throw new RuntimeException();
		}

		@Override
		public Integer[] toArray(Object[] a) {
			throw new RuntimeException();
		}

		@Override
		public boolean add(Integer e) {
			throw new RuntimeException();
		}

		@Override
		public boolean remove(Object o) {
			throw new RuntimeException();
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			throw new RuntimeException();
		}

		@Override
		public boolean addAll(Collection<? extends Integer> c) {
			throw new RuntimeException();
		}

		@Override
		public boolean addAll(int index, Collection<? extends Integer> c) {
			throw new RuntimeException();
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			throw new RuntimeException();
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw new RuntimeException();
		}

		@Override
		public Integer set(int index, Integer element) {
			throw new RuntimeException();
		}

		@Override
		public void add(int index, Integer element) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void clear() {
			throw new RuntimeException();
		}

		@Override
		public Integer get(int index) {
			return index;
		}

		@Override
		public Integer remove(int index) {
			throw new RuntimeException();
		}

		@Override
		public int indexOf(Object o) {
			if (o instanceof Integer) {
				return (Integer) o;
			} else {
				return -1;
			}
		}

		@Override
		public int lastIndexOf(Object o) {
			return indexOf(o);
		}

		@Override
		public ListIterator<Integer> listIterator() {
			throw new RuntimeException();
		}

		@Override
		public ListIterator<Integer> listIterator(int index) {
			throw new RuntimeException();
		}

		@Override
		public List<Integer> subList(int fromIndex, int toIndex) {
			throw new RuntimeException();
		}
		
	}
	
}

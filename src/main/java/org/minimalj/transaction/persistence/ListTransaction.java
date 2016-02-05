package org.minimalj.transaction.persistence;

import java.util.List;

import org.minimalj.backend.Persistence;
import org.minimalj.backend.sql.LazyListAdapter;
import org.minimalj.backend.sql.LazyListAdapter.LazyList;
import org.minimalj.backend.sql.ListTable;
import org.minimalj.backend.sql.SqlPersistence;
import org.minimalj.backend.sql.Table;
import org.minimalj.transaction.PersistenceTransaction;
import org.minimalj.util.SerializationContainer;

public abstract class ListTransaction<T> implements PersistenceTransaction<T> {
	private static final long serialVersionUID = 1L;
	
	protected final LazyList lazyList;

	protected ListTransaction(LazyList lazyList) {
		this.lazyList = lazyList;
	}

	@Override
	public Class<?> getEntityClazz() {
		return lazyList.getElementClass();
	}
	
	// TODO replace with specific implementations
	protected List readAll(SqlPersistence sqlPersistence) {
		Table<?> parentTable = sqlPersistence.getTable(lazyList.getParent().getClass());
		ListTable listTable = parentTable.getListTable(lazyList.getDiscriminator());
		List list = ((LazyListAdapter) listTable).readAll(lazyList.getParent());
		return list;
	}
	
	public static class ReadElementTransaction extends ListTransaction<Object> {
		private static final long serialVersionUID = 1L;
		private final int position;
		
		public ReadElementTransaction(LazyList lazyList, int position) {
			super(lazyList);
			this.position = position;
		}

		@Override
		public Object execute(Persistence persistence) {
			Table<?> parentTable = ((SqlPersistence) persistence).getTable(lazyList.getParent().getClass());
			ListTable listTable = parentTable.getListTable(lazyList.getDiscriminator());
			List list = ((LazyListAdapter) listTable).readAll(lazyList.getParent());
			return list.get(position);
		}
	}
	
	public static class AddTransaction<T> extends ListTransaction<T> {
		private static final long serialVersionUID = 1L;
		protected final Object element;

		public AddTransaction(LazyList lazyList, T element) {
			super(lazyList);
			this.element = SerializationContainer.wrap(element);
		}

		@Override
		public T execute(Persistence persistence) {
			T unwrapped = (T) SerializationContainer.unwrap(element);
			Table<?> parentTable = ((SqlPersistence) persistence).getTable(lazyList.getParent().getClass());
			ListTable listTable = parentTable.getListTable(lazyList.getDiscriminator());
			List list = ((LazyListAdapter) listTable).readAll(lazyList.getParent());
			list.add(unwrapped);
			listTable.update(lazyList.getParent(), list);
			return null;
		}
	}
	
	public static class SetTransaction<T> extends AddTransaction<T> {
		private static final long serialVersionUID = 1L;
		private final int position;

		public SetTransaction(LazyList lazyList, T element, int position) {
			super(lazyList, element);
			this.position = position;
		}

		@Override
		public T execute(Persistence persistence) {
			T unwrapped = (T) SerializationContainer.unwrap(element);
			Table<?> parentTable = ((SqlPersistence) persistence).getTable(lazyList.getParent().getClass());
			ListTable listTable = parentTable.getListTable(lazyList.getDiscriminator());
			List list = ((LazyListAdapter) listTable).readAll(lazyList.getParent());
			list.set(position, unwrapped);
			listTable.update(lazyList.getParent(), list);
			return null;
		}
	}

	public static class RemoveTransaction extends ListTransaction<Integer> {
		private static final long serialVersionUID = 1L;
		private final int position;
		
		public RemoveTransaction(LazyList lazyList, int position) {
			super(lazyList);
			this.position = position;
		}

		@Override
		public Integer execute(Persistence persistence) {
			Table<?> parentTable = ((SqlPersistence) persistence).getTable(lazyList.getParent().getClass());
			ListTable listTable = parentTable.getListTable(lazyList.getDiscriminator());
			List list = ((LazyListAdapter) listTable).readAll(lazyList.getParent());
			list.remove(position);
			listTable.update(lazyList.getParent(), list);
			return null;
		}
	}

	public static class SizeTransaction extends ListTransaction<Integer> {
		private static final long serialVersionUID = 1L;
		
		public SizeTransaction(LazyList lazyList) {
			super(lazyList);
		}

		@Override
		public Integer execute(Persistence persistence) {
			Table<?> parentTable = ((SqlPersistence) persistence).getTable(lazyList.getParent().getClass());
			ListTable listTable = parentTable.getListTable(lazyList.getDiscriminator());
			List list = ((LazyListAdapter) listTable).readAll(lazyList.getParent());
			return list.size();
		}
	}

}
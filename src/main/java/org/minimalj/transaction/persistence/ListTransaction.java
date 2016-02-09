package org.minimalj.transaction.persistence;

import org.minimalj.backend.Persistence;
import org.minimalj.backend.sql.LazyList;
import org.minimalj.backend.sql.SqlPersistence;
import org.minimalj.transaction.PersistenceTransaction;
import org.minimalj.util.SerializationContainer;

public abstract class ListTransaction<PARENT, ELEMENT, RETURN> implements PersistenceTransaction<RETURN> {
	private static final long serialVersionUID = 1L;
	
	protected final LazyList<PARENT, ELEMENT> lazyList;

	protected ListTransaction(LazyList<PARENT, ELEMENT> lazyList) {
		this.lazyList = lazyList;
	}

	@Override
	public Class<?> getEntityClazz() {
		return lazyList.getElementClass();
	}
	
	public static class ReadElementTransaction<PARENT, ELEMENT> extends ListTransaction<PARENT, ELEMENT, ELEMENT> {
		private static final long serialVersionUID = 1L;
		private final int position;
		
		public ReadElementTransaction(LazyList<PARENT, ELEMENT> lazyList, int position) {
			super(lazyList);
			this.position = position;
		}

		@Override
		public ELEMENT execute(Persistence persistence) {
			lazyList.setPersistence((SqlPersistence) persistence);
			return lazyList.get(position);
		}
	}
	
	public static class AddTransaction<PARENT, ELEMENT> extends ListTransaction<PARENT, ELEMENT, Boolean> {
		private static final long serialVersionUID = 1L;
		protected final Object elementWrapped;

		public AddTransaction(LazyList<PARENT, ELEMENT> lazyList, ELEMENT element) {
			super(lazyList);
			this.elementWrapped = SerializationContainer.wrap(element);
		}

		@Override
		public Boolean execute(Persistence persistence) {
			ELEMENT element = (ELEMENT) SerializationContainer.unwrap(elementWrapped);
			lazyList.setPersistence((SqlPersistence) persistence);
			return lazyList.add(element);
		}
	}

	public static class RemoveTransaction<PARENT, ELEMENT> extends ListTransaction<PARENT, ELEMENT, ELEMENT> {
		private static final long serialVersionUID = 1L;
		private final int position;
		
		public RemoveTransaction(LazyList<PARENT, ELEMENT> lazyList, int position) {
			super(lazyList);
			this.position = position;
		}

		@Override
		public ELEMENT execute(Persistence persistence) {
			lazyList.setPersistence((SqlPersistence) persistence);
			return lazyList.remove(position);
		}
	}

	public static class SizeTransaction<PARENT, ELEMENT> extends ListTransaction<PARENT, ELEMENT, Integer> {
		private static final long serialVersionUID = 1L;
		
		public SizeTransaction(LazyList<PARENT, ELEMENT> lazyList) {
			super(lazyList);
		}

		@Override
		public Integer execute(Persistence persistence) {
			lazyList.setPersistence((SqlPersistence) persistence);
			return lazyList.size();
		}
	}

}
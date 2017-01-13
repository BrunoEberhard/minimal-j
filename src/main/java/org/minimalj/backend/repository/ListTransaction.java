package org.minimalj.backend.repository;

import java.util.List;

import org.minimalj.repository.Repository;
import org.minimalj.repository.sql.LazyList;
import org.minimalj.util.SerializationContainer;

public abstract class ListTransaction<PARENT, ELEMENT, RETURN> extends RepositoryTransaction<ELEMENT, RETURN> {
	private static final long serialVersionUID = 1L;
	
	protected final LazyList<PARENT, ELEMENT> lazyList;

	protected ListTransaction(LazyList<PARENT, ELEMENT> lazyList) {
		this.lazyList = lazyList;
	}
	
	@Override
	public Class<ELEMENT> getEntityClazz() {
		return lazyList.getElementClass();
	}

	public static class ReadAllElementsTransaction<PARENT, ELEMENT> extends ListTransaction<PARENT, ELEMENT, List<ELEMENT>> {
		private static final long serialVersionUID = 1L;
		
		public ReadAllElementsTransaction(LazyList<PARENT, ELEMENT> lazyList) {
			super(lazyList);
		}

		@Override
		public List<ELEMENT> execute(Repository repository) {
			lazyList.setRepository(repository);
			return lazyList.getList();
		}
	}
	
	public static class ReadElementTransaction<PARENT, ELEMENT> extends ListTransaction<PARENT, ELEMENT, ELEMENT> {
		private static final long serialVersionUID = 1L;
		private final int position;
		
		public ReadElementTransaction(LazyList<PARENT, ELEMENT> lazyList, int position) {
			super(lazyList);
			this.position = position;
		}

		@Override
		public ELEMENT execute(Repository repository) {
			lazyList.setRepository(repository);
			return lazyList.get(position);
		}
	}
	
	public static class AddTransaction<PARENT, ELEMENT> extends ListTransaction<PARENT, ELEMENT, ELEMENT> {
		private static final long serialVersionUID = 1L;
		protected final Object elementWrapped;

		public AddTransaction(LazyList<PARENT, ELEMENT> lazyList, ELEMENT element) {
			super(lazyList);
			this.elementWrapped = SerializationContainer.wrap(element);
		}

		@Override
		public ELEMENT execute(Repository repository) {
			ELEMENT element = (ELEMENT) SerializationContainer.unwrap(elementWrapped);
			lazyList.setRepository(repository);
			return lazyList.addElement(element);
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
		public ELEMENT execute(Repository repository) {
			lazyList.setRepository(repository);
			return lazyList.remove(position);
		}
	}

}
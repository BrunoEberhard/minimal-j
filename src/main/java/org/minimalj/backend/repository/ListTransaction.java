package org.minimalj.backend.repository;

import java.util.List;

import org.minimalj.repository.Repository;
import org.minimalj.repository.sql.LazyList;

public abstract class ListTransaction<PARENT, ELEMENT, RETURN> extends EntityTransaction<ELEMENT, RETURN> {
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

}
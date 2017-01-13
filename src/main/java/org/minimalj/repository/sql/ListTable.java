package org.minimalj.repository.sql;

import java.util.List;

public interface ListTable<PARENT, ELEMENT> {
	
	public List<ELEMENT> getList(PARENT parent);
	
	public void addList(PARENT parent, List<ELEMENT> objects);

	public void replaceList(PARENT parent, List<ELEMENT> objects);
	
	//
	
	public interface HistorizedListTable<PARENT, ELEMENT> extends ListTable<PARENT, ELEMENT> {
		
		public List<ELEMENT> readAll(PARENT parent, Integer time);
		
		public void readVersions(Object parentId, List<Integer> result);

		@Override
		default void addList(PARENT parent, List<ELEMENT> objects) {
			addList(parent, objects, 0);
		}

		@Override
		default void replaceList(PARENT parent, List<ELEMENT> objects) {
			throw new IllegalArgumentException();
		}

		public void addList(PARENT parent, List<ELEMENT> objects, Integer version);

		public void replaceList(PARENT parent, List<ELEMENT> objects, int version);
	}
}

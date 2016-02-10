package org.minimalj.backend.sql;

import java.util.List;

public interface ListTable<PARENT, ELEMENT> {
	
	public List<ELEMENT> readAll(PARENT parent);
	
	public void addAll(PARENT parent, List<ELEMENT> objects);

	public void replaceAll(PARENT parent, List<ELEMENT> objects);
	
	//
	
	public interface HistorizedListTable<PARENT, ELEMENT> extends ListTable<PARENT, ELEMENT> {
		
		public List<ELEMENT> readAll(PARENT parent, Integer time);
		
		public void readVersions(Object parentId, List<Integer> result);

		@Override
		default void addAll(PARENT parent, List<ELEMENT> objects) {
			addAll(parent, objects, 0);
		}

		@Override
		default void replaceAll(PARENT parent, List<ELEMENT> objects) {
			throw new IllegalArgumentException();
		}

		public void addAll(PARENT parent, List<ELEMENT> objects, Integer version);

		public void replaceAll(PARENT parent, List<ELEMENT> objects, int version);
	}
}

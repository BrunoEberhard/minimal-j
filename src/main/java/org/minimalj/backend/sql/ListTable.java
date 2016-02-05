package org.minimalj.backend.sql;

import java.util.List;

public interface ListTable<PARENT, ELEMENT> {

	public List<ELEMENT> read(PARENT parent);
	
	public void insert(PARENT parent, List<ELEMENT> objects);

	public void update(PARENT parent, List<ELEMENT> objects);
	
	//
	
	public interface HistorizedListTable<PARENT, ELEMENT> extends ListTable<PARENT, ELEMENT> {
		
		public List<ELEMENT> read(PARENT parent, Integer time);
		
		public void readVersions(Object parentId, List<Integer> result);

		@Override
		default void insert(PARENT parent, List<ELEMENT> objects) {
			insert(parent, objects, 0);
		}

		@Override
		default void update(PARENT parent, List<ELEMENT> objects) {
			throw new IllegalArgumentException();
		}

		public void insert(PARENT parent, List<ELEMENT> objects, Integer version);

		public void update(PARENT parent, List<ELEMENT> objects, int version);
	}
	
}
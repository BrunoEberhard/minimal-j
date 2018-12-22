package org.minimalj.repository.sql;

import java.util.List;

public interface HistorizedListTable<PARENT, ELEMENT> extends ListTable<PARENT, ELEMENT> {

	public List<ELEMENT> getList(PARENT parent, Integer time);

	public void replaceList(PARENT parent, List<ELEMENT> objects, int version);

}
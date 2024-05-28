package org.minimalj.repository.sql;

import java.util.List;
import java.util.Map;

public interface HistorizedListTable<PARENT, ELEMENT> extends ListTable<PARENT, ELEMENT> {

	public List<ELEMENT> getList(PARENT parent, Integer time, Map<Class<?>, Map<Object, Object>> loadedReferences);

	public void replaceList(PARENT parent, List<ELEMENT> objects, int version);

}
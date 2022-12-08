package org.minimalj.repository.sql;

import java.util.List;
import java.util.Map;

public interface ListTable<PARENT, ELEMENT> {
	
	public List<ELEMENT> getList(PARENT parent, Map<Class<?>, Map<Object, Object>> loadedReferences);
	
	public void addList(PARENT parent, List<ELEMENT> objects);

	public void replaceList(PARENT parent, List<ELEMENT> objects);
	
}

package org.minimalj.repository.sql;

import java.util.List;

public interface ListTable<PARENT, ELEMENT> {
	
	public List<ELEMENT> getList(PARENT parent);
	
	public void addList(PARENT parent, List<ELEMENT> objects);

	public void replaceList(PARENT parent, List<ELEMENT> objects);
	
}

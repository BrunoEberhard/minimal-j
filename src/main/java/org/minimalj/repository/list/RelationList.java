package org.minimalj.repository.list;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.repository.Repository;
import org.minimalj.util.IdUtils;

public class RelationList<PARENT, ELEMENT> extends AbstractList<ELEMENT> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private transient List<ELEMENT> list;
	private int size;
	
	public RelationList(Repository repository, Class<ELEMENT> elementClass, PARENT parent, String crossName) {
		Object parentId = IdUtils.getId(parent);
		
		list = repository.find(elementClass, new RelationCriteria(crossName, parentId));
		size = list.size();
	}
	
	public boolean isLoaded() {
		return list != null;
	}

	private void loadComplete() {
		if (list instanceof QueryResultList) {
			QueryResultList<ELEMENT> queryResultList = (QueryResultList<ELEMENT>) list;
			list = new ArrayList<ELEMENT>(queryResultList.subList(0, size));
		}
	}
	
	@Override
	public ELEMENT get(int index) {
		return list.get(index);
	}

	@Override
	public List<ELEMENT> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}
	
	@Override
	public int size() {
		if (list instanceof QueryResultList) {
			return size;
		} else {
			return list.size();
		}
	}
	
	@Override
	public ELEMENT set(int index, ELEMENT element) {
		loadComplete();
		return super.set(index, element);
	}
	
	@Override
	public void add(int index, ELEMENT element) {
		loadComplete();
		list.add(index, element);
	}
	
	@Override
	public ELEMENT remove(int index) {
		loadComplete();
		return list.remove(index);
	}
}
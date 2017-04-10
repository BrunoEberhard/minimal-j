package org.minimalj.repository.list;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.repository.Repository;
import org.minimalj.util.IdUtils;

/**
 * The class RelationList is used as a specialized list class for elements with
 * an id. RelationLists load their content lazy. Not all elements are loaded
 * when the main entity is loaded. When a get method is called on the list the
 * requested element is loaded through the backend. If you want avoid too many
 * server roundtrips and you know that a lot of elements will be requested you
 * should use the subList method. The subList method of a RelationList will
 * return a unmodifiable List with all elements of the selected range preloaded.
 * This is how such lists are used by the Frontend.
 * <p>
 * 
 * Normally the use of this class should be completely transparent. But you have
 * to know the additional meaning of the subList method.
 * <p>
 *
 * As soon as this List should be changed it is loaded completely. This could be
 * improved but at the moment it is the easiest way to make sure that the
 * changes as saved when the main entity is saved.
 * <p>
 *
 * @param <PARENT>
 *            Class of the parent entity
 * @param <ELEMENT>
 *            Class of the element entity
 */
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
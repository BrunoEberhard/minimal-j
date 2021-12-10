package org.minimalj.repository.list;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.repository.Repository;
import org.minimalj.util.ClassHolder;
import org.minimalj.util.IdUtils;

/**
 * The class RelationList is used as a specialized list used if a (main) entity
 * has a one-to-many relation to element entities (that have an id of their
 * own).
 * <p>
 * 
 * RelationLists are lazy loading. The element entities are not loaded
 * when the main entity is loaded. When a get method is called on the list the
 * list is loaded through the backend.
 * <p>
 *
 * @param <PARENT>  Class of the parent entity
 * @param <ELEMENT> Class of the element entity
 */
public class RelationList<PARENT, ELEMENT> extends AbstractList<ELEMENT> implements Serializable {
	private static final long serialVersionUID = 1L;

	private transient Repository repository;
	private transient List<ELEMENT> list;

	private ClassHolder<ELEMENT> elementClassHolder;
	private Object parentId;
	private String crossName;

	public RelationList(Repository repository, Class<ELEMENT> elementClass, PARENT parent, String crossName) {
		this.repository = repository;
		this.elementClassHolder = new ClassHolder<>(elementClass);
		this.parentId = IdUtils.getId(parent);
		this.crossName = crossName;
	}

	public void checkLoaded() {
		if (list == null) {
			if (repository != null) {
				list = repository.find(elementClassHolder.getClazz(), new RelationCriteria(crossName, parentId));
			} else {
				list = Backend.find(elementClassHolder.getClazz(), new RelationCriteria(crossName, parentId));
			}
		}
	}

	public boolean isLoaded() {
		return list != null;
	}

	@Override
	public ELEMENT get(int index) {
		checkLoaded();
		return list.get(index);
	}

	@Override
	public List<ELEMENT> subList(int fromIndex, int toIndex) {
		checkLoaded();
		return list.subList(fromIndex, toIndex);
	}

	@Override
	public int size() {
		checkLoaded();
		return list.size();
	}

	@Override
	public ELEMENT set(int index, ELEMENT element) {
		checkLoaded();
		return super.set(index, element);
	}

	@Override
	public void add(int index, ELEMENT element) {
		checkLoaded();
		list.add(index, element);
	}

	@Override
	public ELEMENT remove(int index) {
		checkLoaded();
		return list.remove(index);
	}
}
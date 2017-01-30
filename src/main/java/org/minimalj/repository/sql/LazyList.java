package org.minimalj.repository.sql;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;

import org.minimalj.repository.Repository;
import org.minimalj.util.ClassHolder;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.IdUtils;

public class LazyList<PARENT, ELEMENT> extends AbstractList<ELEMENT> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private transient List<ELEMENT> list;
	
	private transient Repository repository;

	private final ClassHolder<ELEMENT> elementClass;
	
	private final Object parentId;
	
	private final String listName;
	
	public LazyList(Repository repository, Class<ELEMENT> elementClass, PARENT parent, String listName) {
		this.repository = repository;
		this.parentId = IdUtils.getId(parent);
		this.listName = listName;
		this.elementClass = new ClassHolder<>(elementClass);
	}
	
	public void setRepository(Repository repository) {
		this.repository = repository;
	}
	
	private void checkRepository() {
		if (repository == null) {
			throw new IllegalStateException();
		}
	}
	
	public Class<ELEMENT> getElementClass() {
		return elementClass.getClazz();
	}
	
	public Object getParentId() {
		return parentId;
	}
	
	public String getListName() {
		return listName;
	}
	
	public List<ELEMENT> getList() {
		if (list == null) {
			checkRepository();
			return repository.getList(this);
		}
		return list;
	}
	
	@Override
	public int size() {
		return getList().size();
	}
	
	@Override
	public ELEMENT get(int index) {
		return getList().get(index);
	}
	
	@Override
	public boolean add(ELEMENT element) {
		checkRepository();
		ELEMENT savedElement;
		savedElement = repository.add(this, element);
		if (list != null) {
			CloneHelper.deepCopy(savedElement, element);
			list.add(element);
		}
		return true;
	}

	public ELEMENT addElement(ELEMENT element) {
		checkRepository();
		element = repository.add(this, element);
		if (list != null) {
			list.add(element);
		}
		return element;
	}

	@Override
	public ELEMENT remove(int index) {
		checkRepository();
		repository.remove(this, index);
		return null; //
	}
}
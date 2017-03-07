package org.minimalj.repository.sql;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.repository.Repository;
import org.minimalj.util.ClassHolder;
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
	
	public Class<ELEMENT> getElementClass() {
		return elementClass.getClazz();
	}
	
	public Object getParentId() {
		return parentId;
	}
	
	public String getListName() {
		return listName;
	}
	
	public boolean isLoaded() {
		return list != null;
	}
	
	private Repository getRepository() {
		return repository != null ? repository : Backend.getInstance().getRepository();
	}

	public List<ELEMENT> getList() {
		if (!isLoaded()) {
			list = getRepository().getList(this);
		}
		return list;
	}

	@Override
	public ELEMENT get(int index) {
		return getList().get(index);
	}

	@Override
	public int size() {
		return getList().size();
	}
	
	@Override
	public ELEMENT set(int index, ELEMENT element) {
		return super.set(index, element);
	}
	
	@Override
	public void add(int index, ELEMENT element) {
		getList().add(index, element);
	}
	
	@Override
	public ELEMENT remove(int index) {
		return getList().remove(index);
	}
}
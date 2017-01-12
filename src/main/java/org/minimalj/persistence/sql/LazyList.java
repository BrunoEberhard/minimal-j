package org.minimalj.persistence.sql;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.backend.persistence.ListTransaction.AddTransaction;
import org.minimalj.backend.persistence.ListTransaction.ReadAllElementsTransaction;
import org.minimalj.backend.persistence.ListTransaction.RemoveTransaction;
import org.minimalj.persistence.Repository;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.IdUtils;

public class LazyList<PARENT, ELEMENT> extends AbstractList<ELEMENT> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private transient List<ELEMENT> list;
	
	private transient Repository repository;

	private transient Class<ELEMENT> elementClass;
	private final String elementClassName;
	
	private final Object parentId;
	
	private final String listName;
	
	public LazyList(Repository repository, Class<ELEMENT> elementClass, PARENT parent, String listName) {
		this.repository = repository;
		this.parentId = IdUtils.getId(parent);
		this.listName = listName;
		this.elementClass = elementClass;
		this.elementClassName = elementClass.getName();
	}
	
	public void setPersistence(Repository repository) {
		this.repository = repository;
	}
	
	public Class<ELEMENT> getElementClass() {
		if (elementClass == null) {
			try {
				elementClass = (Class<ELEMENT>) Class.forName(elementClassName);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return elementClass;
	}
	
	public Object getParentId() {
		return parentId;
	}
	
	public List<ELEMENT> getList() {
		if (list == null) {
			if (repository != null) {
				return repository.getList(listName, parentId);
			} else {
				list = Backend.execute(new ReadAllElementsTransaction<PARENT, ELEMENT>(this));
			}
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
		ELEMENT savedElement;
		if (repository != null) {
			savedElement = repository.add(listName, parentId, element);
		} else {
			savedElement = Backend.execute(new AddTransaction<PARENT, ELEMENT>(this, element));
		}
		if (list != null) {
			CloneHelper.deepCopy(savedElement, element);
			list.add(element);
		}
		return true;
	}

	public ELEMENT addElement(ELEMENT element) {
		if (repository != null) {
			element = repository.add(listName, parentId, element);
		} else {
			element = Backend.execute(new AddTransaction<PARENT, ELEMENT>(this, element));
		}
		if (list != null) {
			list.add(element);
		}
		return element;
	}

	@Override
	public ELEMENT remove(int index) {
		if (repository != null) {
			repository.remove(listName, parentId, index);
			return null; //
		} else {
			return Backend.execute(new RemoveTransaction<PARENT, ELEMENT>(this, index));
		}
	}
}
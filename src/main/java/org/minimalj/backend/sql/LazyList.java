package org.minimalj.backend.sql;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.backend.Persistence;
import org.minimalj.transaction.persistence.ListTransaction.AddTransaction;
import org.minimalj.transaction.persistence.ListTransaction.ReadAllElementsTransaction;
import org.minimalj.transaction.persistence.ListTransaction.RemoveTransaction;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.IdUtils;

public class LazyList<PARENT, ELEMENT> extends AbstractList<ELEMENT> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private transient List<ELEMENT> list;
	
	private transient Persistence persistence;

	private transient Class<ELEMENT> elementClass;
	private final String elementClassName;
	
	private final Object parentId;
	
	private final String listName;
	
	public LazyList(Persistence persistence, Class<ELEMENT> elementClass, PARENT parent, String listName) {
		this.persistence = persistence;
		this.parentId = IdUtils.getId(parent);
		this.listName = listName;
		this.elementClass = elementClass;
		this.elementClassName = elementClass.getName();
	}
	
	public void setPersistence(Persistence persistence) {
		this.persistence = persistence;
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
			if (persistence != null) {
				return persistence.getList(listName, parentId);
			} else {
				list = Backend.getInstance().execute(new ReadAllElementsTransaction<PARENT, ELEMENT>(this));
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
		if (persistence != null) {
			savedElement = persistence.add(listName, parentId, element);
		} else {
			savedElement = Backend.getInstance().execute(new AddTransaction<PARENT, ELEMENT>(this, element));
		}
		if (list != null) {
			CloneHelper.deepCopy(savedElement, element);
			list.add(element);
		}
		return true;
	}

	public ELEMENT addElement(ELEMENT element) {
		if (persistence != null) {
			element = persistence.add(listName, parentId, element);
		} else {
			element = Backend.getInstance().execute(new AddTransaction<PARENT, ELEMENT>(this, element));
		}
		if (list != null) {
			list.add(element);
		}
		return element;
	}

	@Override
	public ELEMENT remove(int index) {
		if (persistence != null) {
			persistence.remove(listName, parentId, index);
			return null; //
		} else {
			return Backend.getInstance().execute(new RemoveTransaction<PARENT, ELEMENT>(this, index));
		}
	}
}
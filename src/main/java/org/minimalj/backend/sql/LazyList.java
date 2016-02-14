package org.minimalj.backend.sql;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.transaction.PersistenceTransaction;
import org.minimalj.transaction.persistence.ListTransaction.AddTransaction;
import org.minimalj.transaction.persistence.ListTransaction.ReadAllElementsTransaction;
import org.minimalj.transaction.persistence.ListTransaction.RemoveTransaction;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.IdUtils;

public class LazyList<PARENT, ELEMENT> extends AbstractList<ELEMENT> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private transient List<ELEMENT> list;
	
	private transient SqlPersistence persistence;

	private transient Class<ELEMENT> elementClass;
	private final String elementClassName;
	
	private final Object parentId;
	
	private final String tableName;
	
	public LazyList(SqlPersistence persistence, Class<ELEMENT> elementClass, PARENT parent, String tableName) {
		this.persistence = persistence;
		this.parentId = IdUtils.getId(parent);
		this.tableName = tableName;
		this.elementClass = elementClass;
		this.elementClassName = elementClass.getName();
	}
	
	public void setPersistence(SqlPersistence persistence) {
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
	
	private <T> T execute(PersistenceTransaction<T> transaction) {
		if (persistence != null) {
			return persistence.execute(transaction);
		} else {
			return Backend.getInstance().execute(transaction);
		}
	}
	
	public List<ELEMENT> getList() {
		if (list == null) {
			if (persistence != null) {
				ContainingSubTable<PARENT, ELEMENT> subTable = (ContainingSubTable<PARENT, ELEMENT>) persistence.getTableByName().get(tableName);
				list = subTable.readAll(parentId);
			} else {
				list = Backend.getInstance().execute(new ReadAllElementsTransaction<PARENT, ELEMENT>(this));
			}
			for (ELEMENT element : list) {
				IdUtils.setId(element, new ElementId(IdUtils.getId(element), tableName));
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
		if (persistence != null) {
			ContainingSubTable<PARENT, ELEMENT> subTable = (ContainingSubTable<PARENT, ELEMENT>) persistence.getTableByName().get(tableName);
			subTable.add(getParentId(), element);
		} else {
			ELEMENT result = execute(new AddTransaction<PARENT, ELEMENT>(this, element));
			CloneHelper.deepCopy(result, element);
		}
		if (list != null) {
			list.add(element);
		}
		return true;
	}

	public ELEMENT addElement(ELEMENT element) {
		if (persistence != null) {
			ContainingSubTable<PARENT, ELEMENT> subTable = (ContainingSubTable<PARENT, ELEMENT>) persistence.getTableByName().get(tableName);
			element = subTable.addElement(getParentId(), element);
		} else {
			element = execute(new AddTransaction<PARENT, ELEMENT>(this, element));
		}
		if (list != null) {
			list.add(element);
		}
		return element;
	}

	@Override
	public ELEMENT remove(int index) {
		if (persistence != null) {
			throw new RuntimeException("Not yet implemented");
		} else {
			return execute(new RemoveTransaction<PARENT, ELEMENT>(this, index));
		}
	}
}
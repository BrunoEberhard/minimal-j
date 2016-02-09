package org.minimalj.backend.sql;

import java.io.Serializable;
import java.util.AbstractList;

import org.minimalj.backend.Backend;
import org.minimalj.transaction.PersistenceTransaction;
import org.minimalj.transaction.persistence.ListTransaction.AddTransaction;
import org.minimalj.transaction.persistence.ListTransaction.ReadElementTransaction;
import org.minimalj.transaction.persistence.ListTransaction.RemoveTransaction;
import org.minimalj.transaction.persistence.ListTransaction.SizeTransaction;
import org.minimalj.util.IdUtils;

public class LazyList<PARENT, ELEMENT> extends AbstractList<ELEMENT> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private transient SqlPersistence persistence;

	private transient Class<ELEMENT> elementClass;
	private final String elementClassName;
	
//	private transient PARENT parent;
	private transient Class<PARENT> parentClass;
	private final String parentClassName;
	private final Object parentId;
	
	private final String fieldPath;
	
	public LazyList(SqlPersistence persistence, Class<ELEMENT> elementClass, PARENT parent, String fieldPath) {
		this.persistence = persistence;
		this.parentClass = (Class<PARENT>) parent.getClass();
		this.parentClassName = parent.getClass().getName();
		this.parentId = IdUtils.getId(parent);
		this.fieldPath = fieldPath;
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
	
	public String getFieldPath() {
		return fieldPath;
	}
	
	public Class<PARENT> getParentClass() {
		if (parentClass == null) {
			try {
				parentClass = (Class<PARENT>) Class.forName(parentClassName);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return parentClass;
	}
	
	private <T> T execute(PersistenceTransaction<T> transaction) {
		if (persistence != null) {
			return persistence.execute(transaction);
		} else {
			return Backend.getInstance().execute(transaction);
		}
	}
	
	@Override
	public ELEMENT get(int index) {
		if (persistence != null) {
			Table<PARENT> parentTable = persistence.getTable(getParentClass());
			ContainingSubTable<PARENT, ELEMENT> subTable = (ContainingSubTable) parentTable.getSubTable(fieldPath);
			return subTable.read(parentId, index);
		} else {
			return Backend.getInstance().execute(new ReadElementTransaction<PARENT, ELEMENT>(this, index));
		}
	}

	@Override
	public int size() {
		if (persistence != null) {
			Table<PARENT> parentTable = persistence.getTable(getParentClass());
			ContainingSubTable<PARENT, ELEMENT> subTable = (ContainingSubTable) parentTable.getSubTable(fieldPath);
			return subTable.size(getParentId());
		} else {
			return Backend.getInstance().execute(new SizeTransaction<PARENT, ELEMENT>(this));
		}
	}
	
	@Override
	public boolean add(ELEMENT element) {
		if (persistence != null) {
			Table<PARENT> parentTable = persistence.getTable(getParentClass());
			ContainingSubTable<PARENT, ELEMENT> subTable = (ContainingSubTable) parentTable.getSubTable(fieldPath);
			return subTable.add(getParentId(), element);
		} else {
			return execute(new AddTransaction<PARENT, ELEMENT>(this, element));
		}
	}

	@Override
	public ELEMENT remove(int index) {
		execute(new RemoveTransaction(this, index));
		return null;
	}
}
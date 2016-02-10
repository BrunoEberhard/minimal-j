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
	
	@Override
	public ELEMENT get(int index) {
		if (persistence != null) {
			ContainingSubTable<PARENT, ELEMENT> subTable = (ContainingSubTable<PARENT, ELEMENT>) persistence.getTableByName().get(tableName);
			return subTable.read(parentId, index);
		} else {
			return Backend.getInstance().execute(new ReadElementTransaction<PARENT, ELEMENT>(this, index));
		}
	}

	@Override
	public int size() {
		if (persistence != null) {
			ContainingSubTable<PARENT, ELEMENT> subTable = (ContainingSubTable<PARENT, ELEMENT>) persistence.getTableByName().get(tableName);
			return subTable.size(getParentId());
		} else {
			return Backend.getInstance().execute(new SizeTransaction<PARENT, ELEMENT>(this));
		}
	}
	
	@Override
	public boolean add(ELEMENT element) {
		if (persistence != null) {
			ContainingSubTable<PARENT, ELEMENT> subTable = (ContainingSubTable<PARENT, ELEMENT>) persistence.getTableByName().get(tableName);
			return subTable.add(getParentId(), element);
		} else {
			return execute(new AddTransaction<PARENT, ELEMENT>(this, element));
		}
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
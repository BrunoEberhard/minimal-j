package org.minimalj.backend.sql;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.minimalj.backend.Backend;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.transaction.PersistenceTransaction;
import org.minimalj.transaction.criteria.By;
import org.minimalj.transaction.criteria.Criteria;
import org.minimalj.transaction.persistence.ListTransaction.AddTransaction;
import org.minimalj.transaction.persistence.ListTransaction.ReadElementTransaction;
import org.minimalj.transaction.persistence.ListTransaction.RemoveTransaction;
import org.minimalj.transaction.persistence.ListTransaction.SizeTransaction;
import org.minimalj.util.IdUtils;

public class LazyListAdapter<PARENT, ELEMENT> implements ListTable<PARENT, ELEMENT> {
	public static final String PARENT = "parent";
	public static final String DISCRIMINATOR = "discriminator";
	public static final String POSITION = "position";

	private final SqlPersistence sqlPersistence;
	private final Table<ELEMENT> table;
	private final Class<ELEMENT> clazz;
	private final String discriminator;
	private final PropertyInterface parentProperty, discriminatorProperty, positionProperty;
	
	public LazyListAdapter(SqlPersistence sqlPersistence, Table<ELEMENT> table, String discriminator) {
		this.sqlPersistence = sqlPersistence;
		this.table = table;
		this.discriminator = discriminator;
		this.clazz = table.getClazz();
		
		Map<String, PropertyInterface> properties = FlatProperties.getProperties(clazz);
		parentProperty = properties.get(PARENT);
		Objects.nonNull(parentProperty);
		discriminatorProperty = properties.get(DISCRIMINATOR);
		// discriminator is only needed if the object is used in different lists
		// if only used in one place discriminator can be omitted
		positionProperty = properties.get(POSITION);
		Objects.nonNull(positionProperty);
	}
	
	@Override
	public int size(PARENT parent) {
		Criteria criteria = By.field(parentProperty, parent);
		if (discriminatorProperty != null) {
			criteria.and(By.field(discriminatorProperty, discriminator));
		}
		return table.count(criteria);
	}
	
	@Override
	public List<ELEMENT> read(PARENT parent) {
		return new LazyList<PARENT, ELEMENT>(sqlPersistence, clazz, parent, discriminator);
	}
	
	public List<ELEMENT> readAll(PARENT parent) {
		Criteria criteria = By.field(parentProperty, parent);
		if (discriminatorProperty != null) {
			criteria = criteria.and(By.field(discriminatorProperty, discriminator));
		}
		List<ELEMENT> result = table.read(criteria, Integer.MAX_VALUE);
		for (Object object : result) {
			parentProperty.setValue(object, parent);
		}
		return result;
	}

	@Override
	public void addAll(PARENT parent, List<ELEMENT> objects) {
		int existingElements = size(parent);
		prepareElements(parent, objects, discriminator, existingElements);
		for (ELEMENT object : objects) {
			table.insert(object);
		}
	}

	@Override
	// TODO more efficient implementation. For the add - Transaction this is extremly bad implementation
	public void replaceAll(PARENT parent, List<ELEMENT> objects) {
		prepareElements(parent, objects, discriminator, 0);
		Criteria criteria = By.field(parentProperty, parent);
		if (discriminatorProperty != null) {
			criteria.and(By.field(discriminatorProperty, discriminator));
		}
		List existingObjects = table.read(criteria, Integer.MAX_VALUE);
		for (Object existingObject : existingObjects) {
			table.delete(IdUtils.getId(existingObject));
		}
		addAll(parent, objects);
	}
	
	// helper
	
	private void prepareElements(PARENT parent, List<ELEMENT> objects, String discriminator, int startAt) {
		int position = startAt;
		for (ELEMENT object : objects) {
			prepareElement(parent, object, discriminator, position++);
		}
	}

	private void prepareElement(PARENT parent, ELEMENT object, String discriminator, int position) {
		parentProperty.setValue(object, parent);
		if (discriminatorProperty != null) {
			discriminatorProperty.setValue(object, discriminator);
		}
		positionProperty.setValue(object, position++);
	}

	public static class LazyList<PARENT, ELEMENT> extends AbstractList<ELEMENT> implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private transient SqlPersistence persistence;

		private transient Class<ELEMENT> elementClass;
		private final String elementClassName;
		
		private transient PARENT parent;
		private final String parentClassName;
		private final Object parentId;
		
		private final String discriminator;
		
		public LazyList(SqlPersistence persistence, Class<ELEMENT> clazz, PARENT parent, String discriminator) {
			this.persistence = persistence;
			this.parent = parent;
			this.parentClassName = parent.getClass().getName();
			this.parentId = IdUtils.getId(parent);
			this.discriminator = discriminator;
			this.elementClassName = clazz.getName();
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
		
		public String getDiscriminator() {
			return discriminator;
		}
		
		public Object getParent() {
			if (parent == null) {
				try {
					Class<PARENT> parentClass = (Class<PARENT>) Class.forName(parentClassName);
					parent = Backend.read(parentClass, parentId);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
			return parent;
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
			return (ELEMENT) execute(new ReadElementTransaction(this, index));
		}

		@Override
		public int size() {
			return execute(new SizeTransaction(this));
		}
		
		@Override
		public boolean add(ELEMENT element) {
			execute(new AddTransaction<ELEMENT>(this, element));
			return true;
		}

		@Override
		public ELEMENT remove(int index) {
			execute(new RemoveTransaction(this, index));
			return null;
		}
	}
}

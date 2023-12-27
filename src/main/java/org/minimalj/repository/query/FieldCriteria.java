package org.minimalj.repository.query;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.temporal.Temporal;

import org.minimalj.model.Keys;
import org.minimalj.model.ViewUtils;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.Property;
import org.minimalj.repository.sql.EmptyObjects;
import org.minimalj.util.ClassHolder;
import org.minimalj.util.EqualsHelper;
import org.minimalj.util.IdUtils;

public class FieldCriteria extends Criteria implements Serializable {
	private static final long serialVersionUID = 1L;

	private final FieldOperator operator;
	// maybe value should be enforced to Serializable or SerializationContainer should be used
	private final Object value;
	private final ClassHolder<?> classHolder;

	private transient Property property;
	private final String path;

	public FieldCriteria(Object key, Object value) {
		this(key, FieldOperator.equal, value);
	}

	public FieldCriteria(Object key, FieldOperator operator, Object value) {
		this.operator = operator;
		this.value = value;
		
		property = Keys.getProperty(key);
		if (property == null) {
			throw new IllegalArgumentException("Key must be a field from a $ constant or a " + Property.class.getSimpleName());
		}
		assertValidOperator(property, operator);
		assertValidValueClass(property, value);

		this.classHolder = new ClassHolder<>(property.getDeclaringClass());
		this.path = property.getPath();
	}
	
	private FieldCriteria(String path, FieldOperator operator, Object value, ClassHolder<?> classHolder) {
		this.path = path;
		this.operator = operator;
		this.value = value;
		this.classHolder = classHolder;
	}

	private void assertValidOperator(Property property, FieldOperator operator) {
		Class<?> clazz = property.getClazz();
		if (clazz == Integer.class || clazz == Long.class || clazz == BigDecimal.class || Temporal.class.isAssignableFrom(clazz)) return;
		if (operator == FieldOperator.equal || operator == FieldOperator.notEqual)
			return;
		throw new IllegalArgumentException(operator + " only allowed for Integer, Long and BigDecimal fields");
	}

	private void assertValidValueClass(Property property, Object value) {
		if (value != null) {
			if (!ViewUtils.resolve(property.getClazz()).isAssignableFrom(ViewUtils.resolve(value.getClass()))) {
				throw new IllegalArgumentException("Value is " + value.getClass().getName() + " but must be " + property.getClazz().getSimpleName());
			}
		}
	}
	
	public FieldOperator getOperator() {
		return operator;
	}

	public String getPath() {
		return path;
	}

	/*
	 * Only to be used by InMemoryRepository
	 */
	public Property getProperty() {
		if (property == null) {
			property = Properties.getPropertyByPath(classHolder.getClazz(), path);
		}
		return property;
	}
	
	public Object getValue() {
		return value;
	}

	@Override
	public Criteria negate() {
		return new FieldCriteria(path, operator.negate(), value, classHolder);
	}
	
	@Override
	public boolean test(Object object) {
		Property p = getProperty();
		object = p.getValue(object);
		Object value = getValue();
		if (getOperator() == FieldOperator.equal) {
			if (IdUtils.hasId(p.getClazz())) {
				Object objectId = object != null ? IdUtils.getId(object) : null;
				Object valueId = object != null ? IdUtils.getId(value) : null;
				return EqualsHelper.equals(valueId, objectId);
			} else {
				return EqualsHelper.equals(value, object);
			}
		} else {
			if (object == null) {
				if (value == null) {
					return true;
				} else {
					object = EmptyObjects.getEmptyObject(value.getClass());
				}
			} else if (value == null) {
				value = EmptyObjects.getEmptyObject(object.getClass());
			}
			int sign = ((Comparable) object).compareTo(value);
			switch (getOperator()) {
			case less:
				return sign < 0;
			case greater:
				return sign > 0;
			case lessOrEqual:
				return sign <= 0;
			case greaterOrEqual:
				return sign >= 0;
			default:
				throw new RuntimeException();
			}
		}
	}
	
	@Override
	public String toString() {
		Object value = getValue();
		value = value != null && IdUtils.hasId(value.getClass()) ? IdUtils.getId(value) : value;
		return getProperty().getName() + " " + operator.getOperatorAsString() + " " + value;
	}
}

package org.minimalj.repository.query;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.temporal.Temporal;

import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;

public class FieldCriteria extends Criteria implements Serializable {
	private static final long serialVersionUID = 1L;

	private final FieldOperator operator;
	private final Object value;
	
	// The key object is not serializable or later the == operator will not work.
	// But at the moment the only thing needed
	// from the FieldCriteria is the property - path. And that string is serializable
	private final String path;

	public FieldCriteria(Object key, Object value) {
		this(key, FieldOperator.equal, value);
	}

	public FieldCriteria(Object key, FieldOperator operator, Object value) {
		this.operator = operator;
		this.value = value;
		
		PropertyInterface property = Keys.getProperty(key);
		assertValidOperator(property, operator);
		assertValidValueClass(property, value);

		this.path = property.getPath();
	}
	
	FieldCriteria(String path, FieldOperator operator, Object value) {
		this.path = path;
		this.operator = operator;
		this.value = value;
	}

	private void assertValidOperator(PropertyInterface property, FieldOperator operator) {
		Class<?> clazz = property.getClazz();
		if (clazz == Integer.class || clazz == Long.class || clazz == BigDecimal.class || Temporal.class.isAssignableFrom(clazz)) return;
		if (operator == FieldOperator.equal) return;
		throw new IllegalArgumentException(operator + " only allowed for Integer, Long and BigDecimal fields");
	}

	private void assertValidValueClass(PropertyInterface property, Object value) {
		if (value != null) {
			if (!property.getClazz().isAssignableFrom(value.getClass())) {
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

	public Object getValue() {
		return value;
	}

	public Query negate() {
		return new FieldCriteria(path, operator.negate(), value);
	}
}

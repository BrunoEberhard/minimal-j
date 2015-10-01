package org.minimalj.transaction.predicate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.temporal.Temporal;

import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;

public class FieldCriteria extends Criteria implements Serializable {
	private static final long serialVersionUID = 1L;

	private final FieldOperator operator;
	private final Object key;
	private final PropertyInterface property;
	private final Object value;
	
	public FieldCriteria(Object key, Object value) {
		this(key, FieldOperator.equal, value);
	}

	public FieldCriteria(Object key, FieldOperator operator, Object value) {
		this.key = key;
		this.operator = operator;
		this.value = value;
		this.property = Keys.getProperty(key);
		assertValidOperator(property, operator);
		assertValidValueClass(property, value);
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
	
	@Override
	public int getLevel() {
		return 0;
	}
	
	public FieldOperator getOperator() {
		return operator;
	}

	public Object getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}

	public Criteria negate() {
		return new FieldCriteria(key, operator.negate(), value);
	}
}

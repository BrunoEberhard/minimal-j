package org.minimalj.transaction.predicate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.temporal.Temporal;
import java.util.function.Predicate;

import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;

public class FieldPredicate<T> implements Predicate<T>, Serializable {
	private static final long serialVersionUID = 1L;

	private final FieldOperator operator;
	private final Object key;
	private final PropertyInterface property;
	private final Object value;
	
	public FieldPredicate(Object key, Object value) {
		this(key, FieldOperator.equal, value);
	}

	public FieldPredicate(Object key, FieldOperator operator, Object value) {
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
	
	public FieldOperator getOperator() {
		return operator;
	}

	public Object getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public boolean test(T t) {
		Object value = property.getValue(t);
		if (value == null) {
			return this.value != null ^ operator.includesEqual();
		}
		if (value instanceof Number && this.value instanceof Number) {
			// TODO ...
		}
		return true; 
	}
	
	@Override
	public Predicate<T> and(Predicate<? super T> other) {
		if (other != null) {
			return new AndPredicate<T>(this, other);
		} else {
			return this;
		}
	}
}

package org.minimalj.repository.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.minimalj.model.View;
import org.minimalj.model.ViewUtil;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.repository.query.Criteria;
import org.minimalj.repository.query.Criteria.AndCriteria;
import org.minimalj.repository.query.Criteria.CompoundCriteria;
import org.minimalj.repository.query.Criteria.OrCriteria;
import org.minimalj.repository.query.FieldCriteria;
import org.minimalj.repository.query.FieldOperator;
import org.minimalj.repository.query.SearchCriteria;
import org.minimalj.repository.sql.EmptyObjects;
import org.minimalj.util.EqualsHelper;

/*
 * Criterias could implement Predicate. This would be more object oriented than
 * this factory. But it would also pollute those classes with stuff that is normally
 * not necessary part of Criteria. The memory db is not the normal use case. This
 * is why I try to separate this in a factory.
 */
class PredicateFactory {

	static Predicate createPredicate(Class clazz, Criteria query) {
		if (query instanceof FieldCriteria) {
			FieldCriteria fieldCriteria = (FieldCriteria) query;
			PropertyInterface p = fieldCriteria.getProperty();
			return (object) -> {
				object = p.getValue(object);
				Object value = fieldCriteria.getValue();
				if (fieldCriteria.getOperator() == FieldOperator.equal) {
					return EqualsHelper.equals(value, object);
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
					switch (fieldCriteria.getOperator()) {
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
			};
		} else if (query instanceof SearchCriteria) {
			SearchCriteria searchCriteria = (SearchCriteria) query;
			List<PropertyInterface> searchColumns = findSearchColumns(clazz);
			String regex = convertQuery(searchCriteria.getQuery());
			Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			return (object) -> {
				for (PropertyInterface p : searchColumns) {
					Object value = p.getValue(object);
					if (value instanceof String) {
						String s = ((String) value);
						if (pattern.matcher(s).matches()) {
							return !searchCriteria.isNotEqual();
						}
					}
				}
				return searchCriteria.isNotEqual();
			};
		} else if (query instanceof CompoundCriteria) {
			CompoundCriteria compoundCriteria = (CompoundCriteria) query;
			List<Predicate> predicates = new ArrayList<>();
			for (Criteria c : compoundCriteria.getCriterias()) {
				predicates.add(createPredicate(clazz, c));
			}
			if (query instanceof OrCriteria) {
				return (object) -> {
					for (Predicate p : predicates) {
						if (p.test(object)) {
							return true;
						}
					}
					return false;
				};
			} else if (query instanceof AndCriteria) {
				return (object) -> {
					for (Predicate p : predicates) {
						if (!p.test(object)) {
							return false;
						}
					}
					return true;
				};
			}
		}
		return (object) -> true;
	}

	private static List<PropertyInterface> findSearchColumns(Class<?> clazz) {
		if (View.class.isAssignableFrom(clazz)) {
			clazz = ViewUtil.getViewedClass(clazz);
		}
		
		List<PropertyInterface> searchColumns = new ArrayList<>();
		for (PropertyInterface property : FlatProperties.getProperties(clazz).values()) {
			Searched searchable = property.getAnnotation(Searched.class);
			if (searchable != null) {
				searchColumns.add(property);
			}
		}
		if (searchColumns.isEmpty()) {
			throw new IllegalArgumentException("No fields are annotated as 'Searched' in " + clazz.getName());
		}
		return searchColumns;
	}
	
	private static String ESCAPE = ".()+|^$@%\\{},";
	
	private static String convertQuery(String query) {
		query = query.trim();
		StringBuilder sb = new StringBuilder(query.length() + 10);
		sb.append('^');
		for (char c: query.toCharArray()) {
			if (c == '*') {
				sb.append(".*");
			} else if (c == '?') {
				sb.append(".");
			} else if (ESCAPE.indexOf(c) >= 0) {
				sb.append("\\").append(c);
			} else {
				sb.append(c);
			}
		}
		sb.append('$');
		return sb.toString();
	}
}

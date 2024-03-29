package org.minimalj.repository.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.minimalj.model.Keys;
import org.minimalj.model.View;
import org.minimalj.model.ViewUtils;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.Property;

public class SearchCriteria extends Criteria implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String query;
	private final Object[] keys;
	private final boolean notEqual;
	
	private transient List<Property> searchColumns;
	
	public SearchCriteria(String query) {
		this(query, null);
	}

	public SearchCriteria(String query, Object[] keys) {
		this(query, keys, false);
	}
	
	public SearchCriteria(String query, Object[] keys, boolean notEqual) {
		this.keys = keys;
		this.query = query;
		this.notEqual = notEqual;
	}

	public Object[] getKeys() {
		return keys;
	}

	public String getQuery() {
		return query;
	}
	
	public boolean isNotEqual() {
		return notEqual;
	}
	
	@Override
	public Criteria negate() {
		return new SearchCriteria(query, keys, !notEqual);
	}
	
	@Override
	public boolean test(Object object) {
		if (searchColumns == null) {
			searchColumns = findSearchColumns(object.getClass());
		}
		
		String regex = convertQuery(getQuery());
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		for (Property p : searchColumns) {
			Object value = p.getValue(object);
			if (value instanceof String) {
				String s = ((String) value);
				if (pattern.matcher(s).matches()) {
					return !isNotEqual();
				}
			}
		}
		return isNotEqual();
	}
	
	private static List<Property> findSearchColumns(Class<?> clazz) {
		if (View.class.isAssignableFrom(clazz)) {
			clazz = ViewUtils.getViewedClass(clazz);
		}
		
		List<Property> searchColumns = new ArrayList<>();
		// at the moment FlatProperties is used.
		// this supports embedded entities but not nested properties
		// TODO: same behaviour as in a database but maybe not wanted
		for (Property property : FlatProperties.getProperties(clazz).values()) {
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
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("query");
		if (notEqual) {
			s.append(" not");
		}
		s.append(" ").append(query);
		if (keys != null) {
			s.append(" in fields [");
			for (Object key : keys) {
				if (s.length() != 0) {
					s.append(", ");
				}
				s.append(Keys.getProperty(key).getName());
			}
			s.append("]");
		}
		return s.toString();
	}
}

package org.minimalj.repository.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.minimalj.model.annotation.Searched;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.repository.list.RelationCriteria;
import org.minimalj.repository.query.AllCriteria;
import org.minimalj.repository.query.Criteria.AndCriteria;
import org.minimalj.repository.query.Criteria.OrCriteria;
import org.minimalj.repository.query.FieldCriteria;
import org.minimalj.repository.query.FieldOperator;
import org.minimalj.repository.query.Limit;
import org.minimalj.repository.query.Order;
import org.minimalj.repository.query.Query;
import org.minimalj.repository.query.SearchCriteria;
import org.minimalj.util.IdUtils;
import org.minimalj.util.StringUtils;

public class WhereClause<T> {

	private static final String EMPTY_CLAUSE = "1=1";

	private final Table<T> table;
	private String clause = null;
	private List<Object> values = new ArrayList<>();

	public WhereClause(Table<T> table, Query query) {
		this.table = table;

		if (query instanceof AndCriteria) {
			AndCriteria andCriteria = (AndCriteria) query;
			combine(andCriteria.getCriterias(), "AND");
		} else if (query instanceof OrCriteria) {
			OrCriteria orCriteria = (OrCriteria) query;
			combine(orCriteria.getCriterias(), "OR");
		} else if (query instanceof FieldCriteria) {
			FieldCriteria fieldCriteria = (FieldCriteria) query;
			Object value = fieldCriteria.getValue();
			if (value != null && IdUtils.hasId(value.getClass())) {
				value = IdUtils.getId(value);
			}
			whereStatement(fieldCriteria.getPath(), fieldCriteria.getOperator(), value);
		} else if (query instanceof SearchCriteria) {
			SearchCriteria searchCriteria = (SearchCriteria) query;
			String search = convertUserSearch(searchCriteria.getQuery());
			clause = "(";
			List<String> searchColumns = searchCriteria.getKeys() != null ? table.getColumns(searchCriteria.getKeys()) : findSearchColumns();
			boolean first = true;
			for (String column : searchColumns) {
				if (!first) {
					clause += " OR ";
				} else {
					first = false;
				}
				clause += column + (searchCriteria.isNotEqual() ? " NOT" : "") + " LIKE ?";
				values.add(search);
			}
			clause += ")";
			if (table.isHistorized()) {
				clause += " AND historized = 0";
			}
		} else if (query instanceof RelationCriteria) {
			RelationCriteria relationCriteria = (RelationCriteria) query;
			String crossTableName = relationCriteria.getCrossName();
			avoidSqlInjection(crossTableName);
			this.clause = "T.id = C.elementId AND C.id = ? ORDER BY C.position";
			this.values.add(relationCriteria.getRelatedId());
		} else if (query instanceof Limit) {
			Limit limit = (Limit) query;
			WhereClause<T> whereClause = new WhereClause<>(table, limit.getQuery());
			add(whereClause);
			if (!(limit.getQuery() instanceof Order) && !(limit.getQuery() instanceof RelationCriteria)) {
				// MsSql needs an order to allow limits.
				// But h2 doesn't allow "order by x order by y"
				// So only add order by if there is yet no order
				add("ORDER BY ID");
			}
			add(table.sqlRepository.getSqlDialect().limit(limit.getRows(), limit.getOffset()));
		} else if (query instanceof Order) {
			Order order = (Order) query;
			List<Order> orders = new ArrayList<>();
			orders.add(order);
			while (order.getQuery() instanceof Order) {
				order = (Order) order.getQuery();
				orders.add(0, order); // most significant first
			}
			WhereClause<T> whereClause = new WhereClause<>(table, order.getQuery());
			add(whereClause);
			add(order(orders));
		} else if (query instanceof AllCriteria || query == null) {
			this.clause = EMPTY_CLAUSE;
		} else {
			throw new IllegalArgumentException("Unknown criteria: " + query);
		}
	}

	public String getClause() {
		if (!StringUtils.isEmpty(clause)) {
			return " WHERE " + clause;
		} else {
			return "";
		}
	}

	public int getValueCount() {
		return values.size();
	}

	public Object getValue(int index) {
		return values.get(index);
	}

	private void add(WhereClause<T> whereClause) {
		add(whereClause.clause);
		this.values.addAll(whereClause.values);
	}

	private void add(String clause) {
		if (this.clause == null) {
			this.clause = clause;
		} else {
			this.clause += " " + clause;
		}
	}

	private void combine(List<? extends Query> criterias, String operator) {
		if (criterias.isEmpty()) {
        } else if (criterias.size() == 1) {
			add(new WhereClause<>(table, criterias.get(0)));
		} else {
			for (Query criteria : criterias) {
				WhereClause<T> whereClause = new WhereClause<>(table, criteria);
				if (this.clause == null) {
					this.clause = "(";
				} else {
					this.clause += " " + operator + " ";
				}
				add(whereClause);
			}
			this.clause += ")";
		}
	}

	private List<String> findSearchColumns() {
		List<String> searchColumns = new ArrayList<>();
		for (Map.Entry<String, PropertyInterface> entry : table.getColumns().entrySet()) {
			PropertyInterface property = entry.getValue();
			Searched searchable = property.getAnnotation(Searched.class);
			if (searchable != null) {
				searchColumns.add(entry.getKey());
			}
		}
		if (searchColumns.isEmpty()) {
			throw new IllegalArgumentException("No fields are annotated as 'Searched' in " + table.getClazz().getName());
		}
		return searchColumns;
	}

	private String convertUserSearch(String s) {
		return s.replace('*', '%');
	}

	private void whereStatement(final String wholeFieldPath, FieldOperator criteriaOperator, Object value) {
		String criteriaString = criteriaString(criteriaOperator, value);

		this.clause = whereStatement(table, wholeFieldPath, criteriaString);
		if (value != null) {
			this.values.add(value);
		}
	}

	private static String whereStatement(AbstractTable<?> table, String wholeFieldPath, String criteriaString) {
		String fieldPath = wholeFieldPath;
		String column;
		while (true) {
			column = table.findColumn(fieldPath);
			if (column != null)
				break;
			int pos = fieldPath.lastIndexOf('.');
			if (pos < 0)
				throw new IllegalArgumentException("FieldPath " + wholeFieldPath + " not even partially found in " + table.getTableName());
			fieldPath = fieldPath.substring(0, pos);
		}
		if (fieldPath.length() < wholeFieldPath.length()) {
			String restOfFieldPath = wholeFieldPath.substring(fieldPath.length() + 1);
			if ("id".equals(restOfFieldPath)) {
				return column + " " + criteriaString;
			} else {
				PropertyInterface subProperty = table.getColumns().get(column);
				AbstractTable<?> subTable = table.sqlRepository.getAbstractTable(subProperty.getClazz());
				return column + " = (SELECT id FROM " + subTable.getTableName() + " WHERE " + whereStatement(subTable, restOfFieldPath, criteriaString) + ")";
			}
		} else {
			return column + " " + criteriaString;
		}
	}

	private String criteriaString(FieldOperator criteriaOperator, Object value) {
		if (value != null) {
			return criteriaOperator.getOperatorAsString() + " ?";
		} else {
			if (criteriaOperator == FieldOperator.equal) {
				return "IS NULL";
			} else if (criteriaOperator == FieldOperator.notEqual) {
				return "IS NOT NULL";
			} else {
				throw new IllegalArgumentException("null value only allowed for (not) equal operator (" + table.getTableName() + ")");
			}
		}
	}

	private String order(List<Order> orders) {
		StringBuilder s = new StringBuilder();
		for (Order order : orders) {
			if (s.length() == 0) {
				s.append("ORDER BY ");
			} else {
				s.append(", ");
			}
			s.append("id".equalsIgnoreCase(order.getPath()) ? "ID" : table.findColumn(order.getPath()));
			if (!order.isAscending()) {
				s.append(" DESC");
			}
		}
		return s.toString();
	}

	private void avoidSqlInjection(String crossTableName) {
		if (!table.sqlRepository.getTableByName().containsKey(crossTableName)) {
			throw new IllegalArgumentException("Invalid cross name: " + crossTableName);
		}
	}
}

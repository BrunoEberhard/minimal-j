package org.minimalj.repository.sql;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.minimalj.model.Code;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.Keys.MethodProperty;
import org.minimalj.model.Model;
import org.minimalj.model.View;
import org.minimalj.model.ViewUtil;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;

public class SqlHistorizedRepository extends SqlRepository {

	private Map<Class<?>, LinkedHashMap<String, PropertyInterface>> versionColumnsForClass;

	public SqlHistorizedRepository(DataSource dataSource, Class<?>... classes) {
		super(dataSource, classes);
	}

	public SqlHistorizedRepository(Model model) {
		super(model);
	}

	@Override
	protected <U> Table<U> createTable(Class<U> clazz) {
		boolean historized = FieldUtils.hasValidHistorizedField(clazz);
		if (historized) {
			return new HistorizedTable<>(this, clazz);
		} else {
			return super.createTable(clazz);
		}
	}
	
	public LinkedHashMap<String, PropertyInterface> findVersionColumns(Class<?> clazz) {
		if (versionColumnsForClass == null) {
			versionColumnsForClass = new HashMap<>(200);
		}
		if (versionColumnsForClass.containsKey(clazz)) {
			return versionColumnsForClass.get(clazz);
		}
		LinkedHashMap<String, PropertyInterface> columns = findColumns(clazz);
		LinkedHashMap<String, PropertyInterface> versionColumns = new LinkedHashMap<>();
		Set<String> alreadyUsedIdentifiers = new TreeSet<String>(columns.keySet());
		for (Map.Entry<String, PropertyInterface> entry : columns.entrySet()) {
			if (FieldUtils.hasValidHistorizedField(entry.getValue().getClazz())) {
				String fieldName = sqlIdentifier.column(entry.getKey() + "_VERSION", alreadyUsedIdentifiers);
				versionColumns.put(fieldName, entry.getValue());
				alreadyUsedIdentifiers.add(fieldName);
			}
		}
		versionColumnsForClass.put(clazz, versionColumns);
		return versionColumns;
	}

	public <T> List<T> loadHistory(Class<?> clazz, Object id, int maxResult) {
		@SuppressWarnings("unchecked")
		Table<T> table = (Table<T>) getTable(clazz);
		if (table instanceof HistorizedTable) {
			HistorizedTable<T> historizedTable = (HistorizedTable<T>) table;
			int maxVersion = historizedTable.getMaxVersion(id);
			int maxResults = Math.min(maxVersion + 1, maxResult);
			List<T> result = new ArrayList<>(maxResults);
			for (int i = 0; i<maxResults; i++) {
				result.add(historizedTable.read(id, maxVersion - i));
			}
			return result;
		} else {
			throw new IllegalArgumentException(clazz.getSimpleName() + " is not historized");
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T readVersion(Class<T> clazz, Object id, int time) {
		HistorizedTable<T> table = (HistorizedTable<T>) getTable(ViewUtil.resolve(clazz));
		T result = table.read(id, time);
		if (View.class.isAssignableFrom(clazz)) {
			// TODO Historized views are not optimized for read by id and time.
			// The complete object is read and reduced to view.
			// Should not cost too much performance as it's only one entity.
			return ViewUtil.view(result, CloneHelper.newInstance(clazz));
		} else {
			return result;
		}
	}

	@SuppressWarnings("unchecked")
	public <R> R readResultSetRow(Class<R> clazz, ResultSet resultSet, Map<Class<?>, Map<Object, Object>> loadedReferences) throws SQLException {
		if (clazz == Integer.class) {
			return (R) Integer.valueOf(resultSet.getInt(1));
		} else if (clazz == Long.class) {
			return (R) Long.valueOf(resultSet.getLong(1));
		} else if (clazz == BigDecimal.class) {
			return (R) resultSet.getBigDecimal(1);
		} else if (clazz == String.class) {
			return (R) resultSet.getString(1);
		}

		Object id = null;
		Integer position = 0;
		Integer version = 0;
		R result = CloneHelper.newInstance(clazz);

		LinkedHashMap<String, PropertyInterface> columns = findColumns(clazz);
		LinkedHashMap<String, PropertyInterface> versionColumns = findVersionColumns(clazz);

		// first read the resultSet completely then resolve references
		// derby db mixes closing of resultSets.

		Map<PropertyInterface, Object> values = new HashMap<>(resultSet.getMetaData().getColumnCount() * 3);
		Map<PropertyInterface, Integer> versions = new HashMap<>();
		for (int columnIndex = 1; columnIndex <= resultSet.getMetaData().getColumnCount(); columnIndex++) {
			String columnName = resultSet.getMetaData().getColumnName(columnIndex);
			if ("ID".equalsIgnoreCase(columnName)) {
				id = resultSet.getObject(columnIndex);
				IdUtils.setId(result, id);
				continue;
			} else if ("VERSION".equalsIgnoreCase(columnName)) {
				version = resultSet.getInt(columnIndex);
				IdUtils.setVersion(result, version);
				continue;
			} else if ("POSITION".equalsIgnoreCase(columnName)) {
				position = resultSet.getInt(columnIndex);
				continue;
			} else if ("HISTORIZED".equalsIgnoreCase(columnName)) {
				IdUtils.setHistorized(result, resultSet.getInt(columnIndex));
				continue;
			}

			if (versionColumns.containsKey(columnName)) {
				PropertyInterface property = versionColumns.get(columnName);
				versions.put(property, resultSet.getInt(columnIndex));
				continue;
			}

			PropertyInterface property = columns.get(columnName);
			if (property == null)
				continue;

			Class<?> fieldClass = property.getClazz();
			boolean isByteArray = fieldClass.isArray() && fieldClass.getComponentType() == Byte.TYPE;

			Object value = isByteArray ? resultSet.getBytes(columnIndex) : resultSet.getObject(columnIndex);
			if (value == null)
				continue;
			values.put(property, value);
		}

		if (!loadedReferences.containsKey(clazz)) {
			loadedReferences.put(clazz, new HashMap<>());
		}
		Object key = position == null ? id : id + "-" + position;
		key = version == null ? key : key + "-" + version;
		if (loadedReferences.get(clazz).containsKey(key)) {
			return (R) loadedReferences.get(clazz).get(key);
		} else {
			loadedReferences.get(clazz).put(key, result);
		}

		for (Map.Entry<PropertyInterface, Object> entry : values.entrySet()) {
			Object value = entry.getValue();
			PropertyInterface property = entry.getKey();
			if (value != null && !(property instanceof MethodProperty)) {
				Class<?> fieldClass = property.getClazz();
				if (Code.class.isAssignableFrom(fieldClass)) {
					Class<? extends Code> codeClass = (Class<? extends Code>) fieldClass;
					value = getCode(codeClass, value);
				} else if (View.class.isAssignableFrom(fieldClass)) {
					Class<?> viewedClass = ViewUtil.getViewedClass(fieldClass);
					Table<?> referenceTable = getTable(viewedClass);
					value = referenceTable.readView(fieldClass, value, loadedReferences);
				} else if (IdUtils.hasId(fieldClass)) {
					if (versions.containsKey(property)) {
						HistorizedTable<?> referenceTable = (HistorizedTable<?>) getTable(fieldClass);
						value = referenceTable.read(value, versions.get(property), loadedReferences);
					} else if (loadedReferences.containsKey(fieldClass) && loadedReferences.get(fieldClass).containsKey(value)) {
						value = loadedReferences.get(fieldClass).get(value);
					} else {
						Table<?> referenceTable = getTable(fieldClass);
						value = referenceTable.read(value, loadedReferences);
					}
				} else if (AbstractTable.isDependable(property)) {
					value = getTable(fieldClass).read(value);
				} else if (fieldClass == Set.class) {
					Set<?> set = (Set<?>) property.getValue(result);
					Class<?> enumClass = property.getGenericClass();
					EnumUtils.fillSet((int) value, enumClass, set);
					continue; // skip setValue, it's final
				} else {
					value = sqlDialect.convertToFieldClass(fieldClass, value);
				}
				property.setValue(result, value);
			}
		}
		return result;
	}

}

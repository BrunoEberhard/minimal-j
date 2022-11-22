package org.minimalj.repository.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.minimalj.model.Dependable;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.repository.query.By;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;

/**
 * Minimal-J internal
 * 
 * - In this tables the parentId is used as id
 * - An additional column named position
 * - has no sub tables
 */
public class ContainedSubTable<PARENT, ELEMENT extends Dependable<PARENT>> extends Table<ELEMENT> implements ListTable<PARENT, ELEMENT> {

	private final PARENT parentKey;
	private final PropertyInterface parentProperty;
	
	public ContainedSubTable(SqlRepository sqlRepository, Class<ELEMENT> clazz) {
		super(sqlRepository, null, clazz);
		parentKey = Keys.of(clazz).getParent();
		parentProperty = Keys.getProperty(parentKey);
	}
	
	@Override
	public void addList(PARENT parent, List<ELEMENT> objects) {
		try (PreparedStatement insertStatement = createStatement(sqlRepository.getConnection(), insertQuery, false)) {
			for (int position = 0; position<objects.size(); position++) {
				ELEMENT object = objects.get(position);
				parentProperty.setValue(object, parent);
				super.insert(object);
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "addList failed");
		}
	}

	@Override
	public void replaceList(PARENT parent, List<ELEMENT> objects) {
		String query = "SELECT id FROM " + getTableName() + " WHERE " + sqlRepository.name(parentKey) + " = ?";
		List<Long> existingIds = sqlRepository.find(Long.class, query, Integer.MAX_VALUE, IdUtils.getId(parent));

		for (ELEMENT element : objects) {
			parentProperty.setValue(element, parent);
			Object elementId = IdUtils.getId(element);
			if (elementId != null) {
				update(element);
				existingIds.remove(elementId);
			} else {
				IdUtils.setId(element, insert(element));
			}
		}
		if (!existingIds.isEmpty()) {
			query = "DELETE FROM " + getTableName() + " WHERE id = ?";
			for (Long id : existingIds) {
				sqlRepository.execute(query, id);
			}
		}
	}
	
	@Override
	public List<ELEMENT> getList(PARENT parent) {
		// prepare parent as loaded reference to avoid stack overflow
		// by reference cycle between parent and contained element
		Map<Class<?>, Map<Object, Object>> loadedReferences = new HashMap<>();
		Map<Object, Object> loadedReference = new HashMap<>();
		loadedReference.put(IdUtils.getId(parent), parent);
		loadedReferences.put(parent.getClass(), loadedReference);
		
		return find(By.field(parentKey, parent), clazz, loadedReferences);
	}

}
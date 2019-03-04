package org.minimalj.example.openapiclient.page;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.SimpleTableEditorPage;
import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjProperty;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.repository.query.By;
import org.minimalj.util.Codes;

public class EntityTablePage<T> extends SimpleTableEditorPage<T> {
	private final MjEntity entity;
	private final Class<T> clazz;

	public EntityTablePage(MjEntity entity) {
		this.entity = entity;
		this.clazz = (Class<T>) entity.getClazz();
	}

	@Override
	protected Class<T> getClazz() {
		return clazz;
	}

	@Override
	protected Object[] getColumns() {
		List<Object> columnList = new ArrayList<>();
		for (MjProperty property : entity.properties) {
			PropertyInterface p = Properties.getProperty(clazz, property.name);
			columnList.add(p);
		}
		return columnList.toArray();
	}

	@Override
	protected List<T> load() {
		if (Codes.isCode(clazz)) {
			return Codes.get((Class) clazz);
		} else {
			return (List<T>) Backend.read(clazz, By.ALL);
		}
	}

	@Override
	protected Form<T> createForm(boolean editable, boolean newObject) {
		return new EntityForm(entity, editable);
	}

}
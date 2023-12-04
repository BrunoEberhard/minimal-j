package org.minimalj.example.openapiclient.page;

import java.util.Arrays;

import org.minimalj.frontend.form.Form;
import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjProperty;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.Property;

public class EntityForm extends Form {
	private final MjEntity entity;
	private final Class clazz;
	private final int columns;

	public EntityForm(MjEntity entity, boolean editable) {
		super(editable, (entity.properties.size() + 5) / 6);
		this.entity = entity;
		this.clazz = entity.getClazz();
		this.columns = (entity.properties.size() + 5) / 6;

		int column = 0;
		Object[] keys = new Object[0];
		for (MjProperty property : entity.properties) {
			if (column == 0) {
				keys = new Object[columns];
			}
			Property p = Properties.getProperty(clazz, property.name);
			if (p == null) {
				System.out.println("Missing: " + property.name);
			}
			keys[column++] = p;
			if (column == columns) {
				line(keys);
				column = 0;
			}
		}
		if (column > 0) {
			keys = Arrays.copyOfRange(keys, 0, column);
			line(keys);
		}
	}

}

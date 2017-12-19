package org.minimalj.metamodel.page;

import java.text.MessageFormat;
import java.util.List;

import org.minimalj.frontend.page.TablePage;
import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjProperty;
import org.minimalj.metamodel.model.MjProperty.MjPropertyType;
import org.minimalj.util.resources.Resources;

public class PropertyTablePage extends TablePage.TablePageWithDetail<MjProperty, TablePage<?>> {

	private final MjEntity entity;
	
	public PropertyTablePage(MjEntity entity) {
		super(new Object[]{MjProperty.$.name, MjProperty.$.getFormattedType(), MjProperty.$.notEmpty, MjProperty.$.searched, MjProperty.$.materialized});
		this.entity = entity;
	}
	
	@Override
	public String getTitle() {
		return MessageFormat.format(Resources.getString(PropertyTablePage.class), entity.name);
	}
	
	@Override
	protected List<MjProperty> load() {
		return entity.properties;
	}

	@Override
	protected TablePage<?> createDetailPage(MjProperty property) {
		if (property.propertyType.isPrimitive()) {
			return null;
		}
		if (property.propertyType == MjPropertyType.ENUM) {
			return new EnumTablePage(property.type.getClazz());
		} else {
			MjEntity entity = property.getModel().getEntity(property.type.getClazz());
			return new PropertyTablePage(entity);
		}
	}

	@Override
	protected TablePage<?> updateDetailPage(TablePage<?> page, MjProperty property) {
		return createDetailPage(property);
	}

}

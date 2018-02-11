package org.minimalj.metamodel.page;

import java.text.MessageFormat;
import java.util.List;

import org.minimalj.frontend.page.TableDetailPage;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjEntity.MjEntityType;
import org.minimalj.metamodel.model.MjProperty;
import org.minimalj.util.resources.Resources;

public class PropertyTablePage extends TableDetailPage<MjProperty> {

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
	protected TablePage<?> getDetailPage(MjProperty property) {
		if (property.type.type.getJavaClass() != null) {
			return null;
		}
		if (property.type.type == MjEntityType.ENUM) {
			return new EnumTablePage(property.type);
		} else {
			// warum war das so? Wird da irgendeine referenz aufgelöst?
			// MjEntity entity = property.getModel().getEntity(property.type.getClazz());
			return new PropertyTablePage(property.type);
		}
	}

}

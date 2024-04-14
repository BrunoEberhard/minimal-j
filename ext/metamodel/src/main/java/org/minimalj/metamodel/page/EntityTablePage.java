package org.minimalj.metamodel.page;

import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.frontend.page.TableDetailPage;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjModel;

public class EntityTablePage extends TableDetailPage<MjEntity> {

	private final List<MjEntity> entities;

	public EntityTablePage() {
		this(new MjModel(Application.getInstance()));
	}
	
	public EntityTablePage(MjModel model) {
		this.entities = model.entities;
	}
	
	public EntityTablePage(List<MjEntity> entities) {
		this.entities = entities;
	}
	
	@Override
	protected Object[] getColumns() {
		return new Object[] { MjEntity.$.getClassName(), MjEntity.$.type, MjEntity.$.validatable, MjEntity.$.maxInclusive };
	}

	@Override
	protected TablePage<?> getDetailPage(MjEntity entity) {
		if (entity.isEnumeration()) {
			return new EnumTablePage(entity);
		} else {
			return new PropertyTablePage(entity);
		}
	}

	@Override
	protected List<MjEntity> load() {
		return entities;
	}

}

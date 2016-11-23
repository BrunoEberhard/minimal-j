package org.minimalj.metamodel.page;

import java.util.List;

import org.minimalj.frontend.page.TablePage;
import org.minimalj.frontend.page.TablePage.TablePageWithDetail;
import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjModel;
import org.minimalj.metamodel.model.MjEntity.MjEntityType;

public class EntityTablePage extends TablePageWithDetail<MjEntity, TablePage<?>> {

	private final MjModel model;
	
	public EntityTablePage(MjModel model) {
		super(new Object[]{MjEntity.$.name, MjEntity.$.type, MjEntity.$.validatable});
		this.model = model;
	}
	
	@Override
	protected TablePage<?> createDetailPage(MjEntity entity) {
		if (entity.type == MjEntityType.ENUM) {
			return new EnumTablePage(entity.getClazz());
		} else {
			return new PropertyTablePage(entity);
		}
	}

	@Override
	protected TablePage<?> updateDetailPage(TablePage<?> page, MjEntity entity) {
		return createDetailPage(entity);
	}

	@Override
	protected List<MjEntity> load() {
		return model.entities;
	}

}

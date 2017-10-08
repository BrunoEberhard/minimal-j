package org.minimalj.metamodel.page;

import java.util.List;

import org.minimalj.frontend.page.TablePage;
import org.minimalj.frontend.page.TablePage.TablePageWithDetail;
import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjModel;

public class EntityTablePage extends TablePageWithDetail<MjEntity, TablePage<?>> {

	private final MjModel model;
	
	public EntityTablePage(MjModel model) {
		super(new Object[]{MjEntity.$.name, MjEntity.$.type, MjEntity.$.validatable});
		this.model = model;
	}
	
	@Override
	protected TablePage<?> createDetailPage(MjEntity entity) {
		Class<?> clazz = entity.getClazz();
		if (Enum.class.isAssignableFrom(clazz)) {
			return new EnumTablePage(clazz);
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

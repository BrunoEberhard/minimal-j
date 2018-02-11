package org.minimalj.metamodel.page;

import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.frontend.page.TableDetailPage;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjEntity.MjEntityType;
import org.minimalj.metamodel.model.MjModel;

public class EntityTablePage extends TableDetailPage<MjEntity> {

	private final MjModel model;

	public EntityTablePage() {
		this(new MjModel(Application.getInstance()));
	}
	
	public EntityTablePage(MjModel model) {
		super(new Object[]{MjEntity.$.name, MjEntity.$.type, MjEntity.$.validatable});
		this.model = model;
	}
	
	@Override
	protected TablePage<?> getDetailPage(MjEntity entity) {
		if (entity.type == MjEntityType.ENUM) {
			return new EnumTablePage(entity);
		} else {
			return new PropertyTablePage(entity);
		}
	}

	@Override
	protected List<MjEntity> load() {
		return model.entities;
	}

}

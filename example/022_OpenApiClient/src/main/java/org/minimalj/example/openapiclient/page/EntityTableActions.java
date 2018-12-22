package org.minimalj.example.openapiclient.page;

import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.action.Separator;
import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjEntity.MjEntityType;
import org.minimalj.metamodel.model.MjModel;

public class EntityTableActions extends ActionGroup {

	public EntityTableActions(MjModel model) {
		super("Browser");
		for (MjEntity entity : model.entities) {
			if (entity.type == MjEntityType.ENTITY) {
				add(new EntityTablePage<>(entity));
			}
		}
		add(new Separator());
		for (MjEntity entity : model.entities) {
			if (entity.type == MjEntityType.CODE) {
				add(new EntityTablePage<>(entity));
			}
		}
	}

}

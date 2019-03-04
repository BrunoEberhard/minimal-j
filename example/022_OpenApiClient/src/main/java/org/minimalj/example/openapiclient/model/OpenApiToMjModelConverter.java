package org.minimalj.example.openapiclient.model;

import java.util.Map;

import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjModel;
import org.minimalj.rest.openapi.model.OpenAPI;
import org.minimalj.rest.openapi.model.OpenAPI.Schema;

public class OpenApiToMjModelConverter {

	public MjModel convert(OpenAPI api) {
		MjModel model = new MjModel();

		for (Map.Entry<String, Schema> entry : api.components.schemas.entrySet()) {
			MjEntity entity = entity(entry.getKey(), entry.getValue());
			model.addEntity(entity);
		}

		return model;
	}

	private MjEntity entity(String name, Schema value) {
		MjEntity entity = new MjEntity(name);

		return null;
	}
}

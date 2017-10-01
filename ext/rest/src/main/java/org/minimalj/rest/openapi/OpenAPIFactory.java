package org.minimalj.rest.openapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjModel;
import org.minimalj.rest.EntityJsonWriter;
import org.minimalj.rest.openapi.model.OpenAPI;
import org.minimalj.rest.openapi.model.OpenAPI.In;
import org.minimalj.rest.openapi.model.OpenAPI.Operation;
import org.minimalj.rest.openapi.model.OpenAPI.Parameter;
import org.minimalj.rest.openapi.model.OpenAPI.Schema;
import org.minimalj.rest.openapi.model.OpenAPI.Type;
import org.minimalj.util.IdUtils;

public class OpenAPIFactory {
	
	public static String create(Application application) {
		OpenAPI api = new OpenAPI();
		api.info = new OpenAPI.Info();
		api.info.title = application.getName();

		api.paths = new HashMap<>();
		MjModel model = new MjModel(application.getEntityClasses());
		for (MjEntity entity : model.entities) {
			if (IdUtils.hasId(entity.getClazz())) {
				String entityName = entity.getClazz().getSimpleName();
				String entityNamePlural = entityName + "s";
				Map<String, Operation> operations = new HashMap<>();
				
				Operation operation = new Operation();
				operation.summary = "Gets a " + entityName + " by id";
				operation.parameters = new ArrayList<>();
				
				Parameter parameter = new Parameter();
				parameter.name = "id";
				parameter.required = true;
				parameter.in = In.path;
				parameter.description = entityName + " id";
				
				Schema schema = new Schema();
				schema.type = Type.STRING;
				parameter.schema = schema;
				
				operation.parameters.add(parameter);
				
				operations.put("get", operation);
				
				api.paths.put("/" + entityNamePlural + "/{id}", operations);
			}
		}
		
		EntityJsonWriter writer = new EntityJsonWriter();
		return writer.write(api);
	}

	
}

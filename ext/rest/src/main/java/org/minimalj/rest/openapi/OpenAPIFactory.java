package org.minimalj.rest.openapi;

import java.util.HashMap;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjModel;
import org.minimalj.metamodel.model.MjProperty;
import org.minimalj.rest.EntityJsonWriter;
import org.minimalj.rest.openapi.model.OpenAPI;
import org.minimalj.rest.openapi.model.OpenAPI.Content;
import org.minimalj.rest.openapi.model.OpenAPI.In;
import org.minimalj.rest.openapi.model.OpenAPI.Operation;
import org.minimalj.rest.openapi.model.OpenAPI.Parameter;
import org.minimalj.rest.openapi.model.OpenAPI.Property;
import org.minimalj.rest.openapi.model.OpenAPI.Response;
import org.minimalj.rest.openapi.model.OpenAPI.Schema;
import org.minimalj.rest.openapi.model.OpenAPI.Type;
import org.minimalj.util.IdUtils;

public class OpenAPIFactory {
	
	public static String create(Application application) {
		OpenAPI api = new OpenAPI();
		api.openapi = "3.0.0";
		api.info = new OpenAPI.Info();
		api.info.version = "1.0.0";
		api.info.title = application.getName();

		api.components = new OpenAPI.Components();
		MjModel model = new MjModel(application.getEntityClasses());
		for (MjEntity entity : model.entities) {
			if (IdUtils.hasId(entity.getClazz())) {
				String entityName = entity.getClazz().getSimpleName();
				String entityNamePlural = entityName + "s";
				Map<String, Operation> operations = new HashMap<>();
				
				Operation operation = new Operation();
				operation.summary = "Gets a " + entityName + " by id";
				
				Parameter parameter = new Parameter();
				parameter.name = "id";
				parameter.required = true;
				parameter.in = In.path;
				parameter.description = entityName + " id";
				
				Schema schema = new Schema();
				schema.type = Type.STRING;
				parameter.schema = schema;
				
				operation.parameters.add(parameter);
				
				Response response = new Response();
				response.description = "Successful operation";
				
				schema = new Schema();
				schema.$ref = "#/components/schemas/" + entityName;

				Content content = new Content();
				content.schema = schema;

				response.content.put("application/json", content);
				
				operation.responses.put("200", response);
				
				operations.put("get", operation);
				
				api.paths.put("/" + entityNamePlural + "/{id}", operations);
				
				//
				
				schema = schema(entity);
				api.components.schemas.put(entityName, schema);
			}
		}
		
		EntityJsonWriter writer = new EntityJsonWriter();
		return writer.write(api);
	}


	private static Schema schema(MjEntity entity) {
		Schema schema = new Schema();
		// schema.type = OpenAPI.Type.OBJECT;
		for (MjProperty mjProperty : entity.properties) {
			Property property = new Property();
			property.nullable = !mjProperty.notEmpty;
			property.type = type(mjProperty);
			property.format = format(mjProperty);
			schema.properties.put(mjProperty.name, property);
		}
		return schema;
	}
	
	private static OpenAPI.Type type(MjProperty property) {
		switch(property.propertyType) {
		case String : return OpenAPI.Type.STRING;
		case Integer: case Long: return OpenAPI.Type.INTEGER;
		default: return null;
		}
	}
	
	private static String format(MjProperty property) {
		switch(property.propertyType) {
		case String : return null;
		case Integer: return "int32";
		case Long: return "int64";
		default: return null;
		}
	}

}

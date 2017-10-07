package org.minimalj.rest.openapi;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjModel;
import org.minimalj.metamodel.model.MjProperty;
import org.minimalj.metamodel.model.MjProperty.MjPropertyType;
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
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.StringUtils;

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
			String entityName = entity.getClazz().getSimpleName();
			String entityNameLower = StringUtils.lowerFirstChar(entityName);

			if (IdUtils.hasId(entity.getClazz())) {
				Map<String, Operation> operations = new HashMap<>();
				
				Operation operation = operationGetById(entityName);
				operations.put("get", operation);
				api.paths.put("/" + entityNameLower + "s/{id}", operations);
				
				operations = new HashMap<>();
				
				operation = operationGetAll(entityName);
				operations.put("get", operation);
				api.paths.put(entityNameLower + "s/{id}", operations);
			}
			
			Schema schema = schema(entity);
			api.components.schemas.put(entityName, schema);
		}
		
		EntityJsonWriter writer = new EntityJsonWriter();
		return writer.write(api);
	}


	private static Operation operationGetById(String entityName) {
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
		return operation;
	}


	private static Operation operationGetAll(String entityName) {
		Operation operation = new Operation();
		operation.summary = "Gets all " + entityName;
		
		Response response = new Response();
		response.description = "Successful operation";
		
		Schema schema = new Schema();
		schema.type = OpenAPI.Type.ARRAY;
		schema.$ref = "#/components/schemas/" + entityName;

		Content content = new Content();
		content.schema = schema;

		response.content.put("application/json", content);
		
		operation.responses.put("200", response);
		return operation;
	}
	
	private static Schema schema(MjEntity entity) {
		Schema schema = new Schema();

		Property property = new Property();
		property.nullable = true;
		property.type = OpenAPI.Type.STRING;
		schema.properties.put("id", property);
		
		boolean hasVersion = FieldUtils.hasValidVersionfield(entity.getClazz());
		if (hasVersion) {
			property = new Property();
			property.nullable = true;
			property.type = OpenAPI.Type.INTEGER;
			schema.properties.put("version", property);
		}
		
		boolean historized = FieldUtils.hasValidHistorizedField(entity.getClazz());
		if (historized) {
			property = new Property();
			property.nullable = true;
			property.type = OpenAPI.Type.BOOLEAN;
			schema.properties.put("historized", property);
		}
		
		for (MjProperty mjProperty : entity.properties) {
			property = new Property();
			property.nullable = mjProperty.notEmpty ? null : true; // nullable false is default, omit
			property.type = type(mjProperty);
			property.$ref = ref(mjProperty);
			property.format = format(mjProperty);
			property.items = items(mjProperty);
			schema.properties.put(mjProperty.name, property);
		}
		return schema;
	}
	
	private static OpenAPI.Type type(MjProperty property) {
		switch (property.propertyType) {
		case String:
		case LocalDate:
		case LocalDateTime:
		case LocalTime:
		case REFERENCE:
			return OpenAPI.Type.STRING;
		case Integer:
		case Long:
			return OpenAPI.Type.INTEGER;
		case LIST:
		case ENUM_SET:
			return OpenAPI.Type.ARRAY;
		case ENUM:
			return OpenAPI.Type.STRING;
		case INLINE:
		case DEPENDABLE:
			return OpenAPI.Type.OBJECT;

		default: return null;
		}
	}
	
	private static String ref(MjProperty property) {
		if (property.propertyType == MjPropertyType.INLINE ||property.propertyType == MjPropertyType.DEPENDABLE) {
			return "#/components/schemas/" + property.type.getClazz().getSimpleName();
		} else {
			return null;
		}
	}
	
	private static String format(MjProperty property) {
		switch(property.propertyType) {
		case String : return null;
		case Integer: return "int32";
		case Long: return "int64";
		case LocalDate: return "date";
		case LocalTime: return null; // local time does not exist in json
		case LocalDateTime: return "date-time";
		default: return null;
		}
	}

	private static Schema items(MjProperty property) {
		if (property.propertyType == MjPropertyType.LIST && IdUtils.hasId(property.type.getClazz())) {
			Schema schema = new Schema();
			schema.type = OpenAPI.Type.STRING;
			return schema;
		}
		if (property.propertyType == MjPropertyType.LIST || property.propertyType == MjPropertyType.ENUM_SET) {
			Schema schema = new Schema();
			schema.$ref = "#/components/schemas/" + property.type.getClazz().getSimpleName();
			return schema;
		} else {
			return null;
		}
	}

}

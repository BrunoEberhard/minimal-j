package org.minimalj.model;

import java.util.Map;

import org.minimalj.model.properties.FlatProperties;

public class ViewUtil {

	public static <T> T view(Object completeObject, T viewObject) {
		if (completeObject == null) return null;
		
		Map<String, PropertyInterface> propertiesOfCompleteObject = FlatProperties.getProperties(completeObject.getClass());
		Map<String, PropertyInterface> properties = FlatProperties.getProperties(viewObject.getClass());

		for (Map.Entry<String, PropertyInterface> entry : properties.entrySet()) {
			PropertyInterface property = propertiesOfCompleteObject.get(entry.getKey());
			Object value = property.getValue(completeObject);
			entry.getValue().setValue(viewObject, value);
		}
		return viewObject;
	}
	
}

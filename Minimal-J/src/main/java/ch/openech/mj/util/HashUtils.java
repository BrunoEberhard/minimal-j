package ch.openech.mj.util;

import java.util.Map;

import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.model.properties.FlatProperties;

public class HashUtils {

	
	public static int getHash(Object object) {
		if (object == null) return 0;
		
		final int prime = 31;
		int result = 1;
		Map<String, PropertyInterface> properties = FlatProperties.getProperties(object.getClass());
		for (Map.Entry<String, PropertyInterface> entry : properties.entrySet()) {
			PropertyInterface property = entry.getValue();
			Object value = property.getValue(object);
			if (value != null) {
				result = prime * result + entry.getKey().hashCode();
				result = prime * result + value.hashCode();
			}
		}
		return result;
	}
	
}

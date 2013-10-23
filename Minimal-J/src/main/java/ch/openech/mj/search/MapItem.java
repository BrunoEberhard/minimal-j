package ch.openech.mj.search;

import java.util.HashMap;
import java.util.Map;

public class MapItem implements Item {

	private final Object id;
	private final Map<Object, Object> values = new HashMap<>();
	
	public MapItem(Object id) {
		this.id = id;
	}
	
	public Object getId() {
		return id;
	}
	
	public void setValue(Object key, Object value) {
		values.put(key, value);
	}
	
	public Object getValue(Object key) {
		return values.get(key);
	}
}

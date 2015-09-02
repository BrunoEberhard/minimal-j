package org.minimalj.frontend.impl.json;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;

public class JsonCombobox<T> extends JsonInputComponent<T> {

	private final Map<String, T> objectById = new LinkedHashMap<>();
	private final Map<String, Object> options = new LinkedHashMap<>();
	
	public JsonCombobox(List<T> objects, InputComponentListener changeListener) {
		super("Combobox", changeListener);
		
		for (T object : objects) {
			String id = UUID.randomUUID().toString();
			if (object instanceof Rendering) {
				Rendering rendering = (Rendering) object;
				String text = rendering.render(RenderType.PLAIN_TEXT, Locale.getDefault());
				options.put(id, text);
			}
			objectById.put(id, object);
		}
		put("options", options);
	}

	@Override
	public void setValue(T object) {
		for (Map.Entry<String, T> entry : objectById.entrySet()) {
			if (Objects.equals(entry.getValue(), object)) {
				put(VALUE, entry.getKey());
				return;
			}
		} 
		put(VALUE, null);
	}

	@Override
	public T getValue() {
		String selectedId = (String) get(VALUE);
		if (selectedId != null) {
			return objectById.get(selectedId);
		} else {
			return null;
		}
	}
}

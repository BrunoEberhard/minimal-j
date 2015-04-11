package org.minimalj.frontend.json;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.minimalj.frontend.toolkit.ClientToolkit.InputComponentListener;
import org.minimalj.frontend.toolkit.ComboBox;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;

public class JsonCombobox<T> extends JsonValueComponent implements ComboBox<T> {

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
	public void setSelectedObject(T object) {
	}

	@Override
	public T getSelectedObject() {
		return null;
	}

	@Override
	public void setEditable(boolean editable) {
		put(EDITABLE, editable);
	}
}

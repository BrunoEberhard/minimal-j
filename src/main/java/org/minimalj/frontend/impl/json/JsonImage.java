package org.minimalj.frontend.impl.json;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;

public class JsonImage extends JsonInputComponent<byte[]> implements Input<byte[]> {

	public JsonImage(InputComponentListener changeListener) {
		super("Image", changeListener);
		setEditable(changeListener != null);
	}

	@Override
	public void setValue(byte[] value) {
		if (value != null) {
			put(VALUE, Base64.getEncoder().encodeToString(value));
		} else {
			put(VALUE, null);
		}
	}

	@Override
	void changedValue(Object value) {
		if (value instanceof List) {
			List<?> list = (List<?>) value;
			for (Object o : list) {
				if (o instanceof Map) {
					@SuppressWarnings("unchecked")
					Map<String, Object> map = (Map<String, Object>) o;
					if (map.containsKey("content")) {
						String contentString = (String) map.get("content");
						super.changedValue(contentString);
					}
				} else {
					throw new IllegalArgumentException("Should be a Map: " + o);
				}
			}
		} else {
			throw new IllegalArgumentException("Should be a List: " + value);
		}
	}

	@Override
	public byte[] getValue() {
		if (get(VALUE) != null) {
			return Base64.getDecoder().decode((String) get(VALUE));
		} else {
			return null;
		}
	}

}

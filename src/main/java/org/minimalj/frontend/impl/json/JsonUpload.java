package org.minimalj.frontend.impl.json;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.NamedFile;

public class JsonUpload extends JsonInputComponent<NamedFile[]> implements Input<NamedFile[]> {

	public JsonUpload(InputComponentListener changeListener, boolean multiple) {
		super("Upload", changeListener);
		setEditable(changeListener != null);
		put("multiple", multiple);
	}

	@Override
	public void setValue(NamedFile[] value) {
		put(VALUE, value);
	}
	
	@Override
	void changedValue(Object value) {
		if (value instanceof List) {
			List<?> list = (List<?>) value;
			List<NamedFile> namedFileList = new ArrayList<>();
			for (Object o : list) {
				if (o instanceof Map) {
					@SuppressWarnings("unchecked")
					Map<String, Object> map = (Map<String, Object>) o;
					if (map.containsKey("content")) {
						String contentString = (String) map.get("content");
						NamedFile namedFile = new NamedFile();
						namedFile.content = Base64.getDecoder().decode(contentString);
						namedFile.name = (String) map.get("name");
						namedFileList.add(namedFile);
					}
				} else {
					throw new IllegalArgumentException("Should be a Map: " + o);
				}
				super.changedValue(namedFileList.toArray(new NamedFile[namedFileList.size()]));
			}
		} else {
			throw new IllegalArgumentException("Should be a List: " + value);
		}
	}

	@Override
	public NamedFile[] getValue() {
		return (NamedFile[]) get(VALUE);
	}

}

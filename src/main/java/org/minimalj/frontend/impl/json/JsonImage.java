package org.minimalj.frontend.impl.json;

import java.util.Base64;

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
	public byte[] getValue() {
		if (get(VALUE) != null) {
			return Base64.getDecoder().decode((String) get(VALUE));
		} else {
			return null;
		}
	}

}

package org.minimalj.frontend.form.element;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.Size;

public class ImageFormElement extends AbstractFormElement<byte[]> {

	private final Input<byte[]> input;
	
	public ImageFormElement(Object key) {
		super(key);
		input = Frontend.getInstance().createImage(Size.MEDIUM, new ImageFieldChangeListener());
	}

	@Override
	public void setValue(byte[] object) {
		input.setValue(object);
	}

	@Override
	public byte[] getValue() {
		return input.getValue();
	}

	@Override
	public IComponent getComponent() {
		return input;
	}
	
	private class ImageFieldChangeListener implements InputComponentListener {
		@Override
		public void changed(IComponent source) {
			// TODO image validation
			fireChange();
		}
	}
}
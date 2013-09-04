package ch.openech.mj.lanterna.toolkit;

import ch.openech.mj.lanterna.component.Select;
import ch.openech.mj.toolkit.ClientToolkit.InputComponentListener;
import ch.openech.mj.toolkit.ComboBox;

import com.googlecode.lanterna.input.Key;

public class LanternaComboBox<T> extends Select<T> implements ComboBox<T> {

	private final InputComponentListener changeListener;
	
	public LanternaComboBox(InputComponentListener changeListener) {
		this.changeListener = changeListener;
	}

	private void fireChangeEvent() {
		changeListener.changed(LanternaComboBox.this);
	}
	
	@Override
	public Result keyboardInteraction(Key key) {
		Result result = super.keyboardInteraction(key);
		if (result != Result.EVENT_NOT_HANDLED) {
			fireChangeEvent();
		}
		return result;
	}
}

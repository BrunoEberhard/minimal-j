package org.minimalj.frontend.editor;

import java.util.function.Consumer;

public interface Result<RESULT> {

	public void setFinishedListener(Consumer<RESULT> finishedListener);
	
}

package org.minimalj.frontend.toolkit;

import org.minimalj.frontend.toolkit.ClientToolkit.Input;


public interface TextField extends Input<String> {
	
	public void setCommitListener(Runnable runnable);

}

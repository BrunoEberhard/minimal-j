package org.minimalj.frontend.toolkit;

import org.minimalj.frontend.toolkit.ClientToolkit.IContext;


public interface IDialog extends IContext {

	public void setCloseListener(CloseListener closeListener);

	public interface CloseListener {
		
		public boolean close();
	}

	void openDialog();

	void closeDialog();
	
}

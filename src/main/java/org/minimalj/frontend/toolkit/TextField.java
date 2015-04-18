package org.minimalj.frontend.toolkit;

import org.minimalj.frontend.toolkit.ClientToolkit.Input;



public interface TextField extends Input<String> {
	
//	public default void setValue(Object value) {
//		setValue((String) value);
//	}
//	
//	public default String getValue() {
//		return (String) getValue();
//	}
//	
//	public void setValue(String text);
//
//	public String getValue();

	public void setEditable(boolean editable);
	
	public void setFocusListener(IFocusListener focusListener);
	
	public void setCommitListener(Runnable runnable);

}

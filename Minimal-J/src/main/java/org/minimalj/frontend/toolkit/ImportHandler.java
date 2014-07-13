package org.minimalj.frontend.toolkit;

import java.io.InputStream;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;


public interface ImportHandler extends IComponent {

	public void imprt(InputStream stream);
	
}

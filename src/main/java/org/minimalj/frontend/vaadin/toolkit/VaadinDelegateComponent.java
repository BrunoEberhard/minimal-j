package org.minimalj.frontend.vaadin.toolkit;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;

import com.vaadin.ui.Component;

/**
 * The vaadin text components have a problem to implement Input&lt;String&gt; because
 * of a name clash in getValue method. This delegate is a work around to this.<p>
 * 
 * This is acceptable as Vaadin 6 will soon be deprecated as FrontEnd
 */
public interface VaadinDelegateComponent extends IComponent {

	public Component getDelegate();
	
}

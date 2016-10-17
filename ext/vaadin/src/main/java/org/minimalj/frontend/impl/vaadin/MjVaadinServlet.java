package org.minimalj.frontend.impl.vaadin;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.minimalj.application.Application;

import com.vaadin.server.VaadinServlet;

public abstract class MjVaadinServlet extends VaadinServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		Application.setInstance(createAppliction());
	}

	protected abstract Application createAppliction();

}

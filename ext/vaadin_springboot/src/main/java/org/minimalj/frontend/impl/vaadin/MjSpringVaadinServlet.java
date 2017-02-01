package org.minimalj.frontend.impl.vaadin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.minimalj.security.Subject;
import org.springframework.stereotype.Component;

import com.vaadin.server.VaadinServletRequest;
import com.vaadin.spring.server.SpringVaadinServlet;

@Component("vaadinServlet")
public class MjSpringVaadinServlet extends SpringVaadinServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected VaadinServletRequest createVaadinRequest(HttpServletRequest request) {
		VaadinServletRequest vaadinServletRequest = super.createVaadinRequest(request);
		Subject.setCurrent((Subject) vaadinServletRequest.getWrappedSession().getAttribute("subject"));
		return vaadinServletRequest;
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			super.service(request, response);
		} finally {
			Subject.setCurrent(null);
		}
	}
}

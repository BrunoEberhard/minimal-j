package org.minimalj.frontend.impl.vaadin;

import java.io.IOException;

import org.minimalj.security.Subject;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.SynchronizedRequestHandler;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;

public class VaadinSubjectRequestHandler extends SynchronizedRequestHandler implements VaadinServiceInitListener {

	@Override
	public void serviceInit(ServiceInitEvent event) {
		event.addRequestHandler(this);
	}

	@Override
	public boolean synchronizedHandleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response) throws IOException {
		Subject.setCurrent((Subject) session.getAttribute("subject"));
		System.out.println(request.getPathInfo());
		return false;
	}
	
}

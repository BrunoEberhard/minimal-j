package org.minimalj.frontend.impl.vaadin;

import java.io.IOException;

import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.security.Subject;
import org.minimalj.util.LocaleContext;
import org.minimalj.util.LocaleContext.AcceptedLanguageLocaleSupplier;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.SynchronizedRequestHandler;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.VaadinSession;

public class VaadinSubjectRequestHandler extends SynchronizedRequestHandler implements VaadinServiceInitListener {
	private static final long serialVersionUID = 1L;

	@Override
	public void serviceInit(ServiceInitEvent event) {
		event.addRequestHandler(this);
	}

	@Override
	public boolean synchronizedHandleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response) throws IOException {
		Subject.setCurrent((Subject) session.getAttribute("subject"));
		LocaleContext.setLocale(new AcceptedLanguageLocaleSupplier(request.getHeader(AcceptedLanguageLocaleSupplier.ACCEPTED_LANGUAGE_HEADER)));
		
		if (!Vaadin.hasUrlMapping()) {
			HttpServletHttpExchange exchange = new HttpServletHttpExchange(((VaadinServletRequest) request).getHttpServletRequest(), ((VaadinServletResponse) response).getHttpServletResponse());
			return WebApplication.callHandlers(exchange);
		}
		return false;
	}
	
}

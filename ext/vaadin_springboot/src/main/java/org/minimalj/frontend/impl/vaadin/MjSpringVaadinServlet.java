package org.minimalj.frontend.impl.vaadin;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.minimalj.security.Subject;
import org.springframework.context.annotation.Bean;
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
	
	@Bean
	public Filter getFilter() {
		return new Filter() {
			@Override
			public void init(FilterConfig filterConfig) throws ServletException {
			}
			
			@Override
			public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
				if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
					HttpServletRequest httpServletRequest = (HttpServletRequest) request;
					HttpServletResponse httpServletResponse = (HttpServletResponse) response;
					String uri = httpServletRequest.getRequestURI();
					if (!uri.startsWith("/vaadinServlet/") && !uri.startsWith("/VAADIN/") && !uri.equals("/")) {
						httpServletRequest.getSession().setAttribute("path", uri);
						httpServletResponse.sendRedirect("/");
						return;
					}
				}
				chain.doFilter(request, response);
			}
			
			@Override
			public void destroy() {
			}
		};
	}
}

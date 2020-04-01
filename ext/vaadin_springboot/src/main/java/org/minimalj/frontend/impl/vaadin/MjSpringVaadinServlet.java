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

import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.security.Subject;
import org.minimalj.util.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.vaadin.spring.server.SpringVaadinServlet;

@Component("vaadinServlet")
public class MjSpringVaadinServlet extends SpringVaadinServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			Subject.setCurrent((Subject) request.getSession().getAttribute("subject"));
			super.service(request, response);
		} finally {
			Subject.setCurrent(null);
		}
	}
	
	@Bean
	public Filter getFilter() {
		return new Filter() {
			private final String vaadinServlet = !StringUtils.isEmpty(WebApplication.mjHandlerPath()) ? WebApplication.mjHandlerPath() : "/vaadinServlet/";

			@Override
			public void init(FilterConfig filterConfig) throws ServletException {
			}

			@Override
			public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
				if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
					HttpServletRequest httpServletRequest = (HttpServletRequest) request;
					HttpServletResponse httpServletResponse = (HttpServletResponse) response;
					String uri = httpServletRequest.getRequestURI();
//					if (uri.startsWith(vaadinServlet) && !uri.equals(vaadinServlet) && !uri.equals("/")) {
//						httpServletRequest.getSession().setAttribute("path", uri);
//						httpServletResponse.sendRedirect(vaadinServlet);
//						return;
//					}
					
//					Einen ViewProvider anmelden statt das hier?
							
					System.out.println(uri);
				}
				chain.doFilter(request, response);
			}

			@Override
			public void destroy() {
			}
		};
	}
}

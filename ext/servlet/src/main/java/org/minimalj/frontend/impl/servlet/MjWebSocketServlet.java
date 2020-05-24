package org.minimalj.frontend.impl.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import org.minimalj.frontend.impl.json.JsonPageManager;
import org.minimalj.frontend.impl.json.JsonPush;
import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.frontend.impl.web.WebServer;

public class MjWebSocketServlet {
	private static final Logger logger = Logger.getLogger(MjWebSocketServlet.class.getName());

	private Map<Session, JsonPageManager> pageManagers = new HashMap<>();
	
    public MjWebSocketServlet() {
    }

    @OnOpen
    public void start(Session session) {
		JsonPageManager pageManager = new JsonPageManager(WebServer.useWebSocket ? new MjWebSocketServletPush(session) : null);
		pageManagers.put(session, pageManager);
    }

    @OnClose
    public void end(Session session) {
    	pageManagers.remove(session);
    }

    @OnMessage
    public void incoming(String message, Session session) {
    	JsonPageManager pageManager = pageManagers.get(session);
    	String result = pageManager.handle(message);
    	try{
    		session.getBasicRemote().sendText(result);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Send response failed", e);
		}
    }

    @OnError
    public void onError(Throwable t) throws Throwable {
        logger.severe("Error: " + t);
    }

	public static class MjWebSocketServletPush implements JsonPush {
		private final Session session;

		public MjWebSocketServletPush(Session session) {
			this.session = session;
		}

		@Override
		public void push(String message) {
			try {
				session.getBasicRemote().sendText(message);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Push failed", e);
			}
		}
	}

	static void addWebSocketEndpoint(ServletConfig servletConfig) {
		ServletContext servletContext = servletConfig.getServletContext();
		Object serverContainerAttribute = servletContext.getAttribute("javax.websocket.server.ServerContainer");
		if (serverContainerAttribute instanceof ServerContainer) {
			ServerContainer serverContainer = (ServerContainer) serverContainerAttribute;
			for (String mapping : servletConfig.getServletContext().getServletRegistration(servletConfig.getServletName()).getMappings()) {
				if (mapping.endsWith("*")) {
					mapping = mapping.substring(0, mapping.length() - 1);
				}
				if (mapping.endsWith("/")) {
					mapping = mapping.substring(0, mapping.length() - 1);
				}
				ServerEndpointConfig config = ServerEndpointConfig.Builder.create(MjWebSocketServlet.class, mapping + WebApplication.mjHandlerPath() + "ws").build();
				try {
					serverContainer.addEndpoint(config);
				} catch (DeploymentException e) {
					throw new RuntimeException(e);
				}
			}
		} else {
			logger.warning("WebSockets should be activated but no ServerContainer available on server");
		}
	}
}
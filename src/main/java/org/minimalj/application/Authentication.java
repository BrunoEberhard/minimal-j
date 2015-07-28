package org.minimalj.application;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.minimalj.backend.Backend;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.StringUtils;

public abstract class Authentication {
	private static final Logger logger = Logger.getLogger(Authentication.class.getName());

	private static IAuthentication instance;
	
	private static IAuthentication createAuthentication() {
		String securityClassName = System.getProperty("MjAuthentication");
		if (!StringUtils.isBlank(securityClassName)) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends IAuthentication> authenticationClass = (Class<? extends IAuthentication>) Class.forName(securityClassName);
				IAuthentication authentication = authenticationClass.newInstance();
				Authentication.setInstance(authentication);
			} catch (Exception x) {
				throw new LoggingRuntimeException(x, logger, "Set authentication failed");
			}
		} 

		if (Backend.getInstance() instanceof IAuthentication) {
			IAuthentication authentication = (IAuthentication) Backend.getInstance();
			Authentication.setInstance(authentication);
		}
		
		return null;
	}

	public static void setInstance(IAuthentication security) {
		if (Authentication.instance != null) {
			throw new IllegalStateException("Security cannot be changed");
		}		
		if (security == null) {
			throw new IllegalArgumentException("Security cannot be null");
		}
		instance = security;
	}
	
	public static IAuthentication getInstance() {
		if (instance == null) {
			instance = createAuthentication();
		}
		return instance;
	}
	
	//
	
	public static void checkPermission(String... accessRoles) {
		if (accessRoles != null) {
			Subject subject = Subject.get();
			List<String> roles =  subject != null ? subject.getRoles() : Collections.emptyList();
			for (String accessRole : accessRoles) {
				if (roles.contains(accessRole)) {
					return;
				}
			}
			throw new RuntimeException("Security exception");
		}
	}

	//
	
	public static interface IAuthentication {
		
		public abstract Subject login(String user, Serializable authentication);
		
		public abstract void logout();

	}
	
}

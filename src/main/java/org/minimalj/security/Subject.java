package org.minimalj.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.page.PageBrowser;
import org.minimalj.transaction.Role;
import org.minimalj.transaction.Transaction;

public class Subject implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private Serializable token;
	
	private final List<String> roles = new ArrayList<>();

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public Serializable getToken() {
		return token;
	}
	
	public void setToken(Serializable token) {
		this.token = token;
	}
	
	public List<String> getRoles() {
		return roles;
	}

	public boolean hasPermission(String... accessRoles) {
		for (String accessRole : accessRoles) {
			if (roles.contains(accessRole)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean hasPermission(Transaction<?> transaction) {
		Role role = transaction.getClass().getAnnotation(Role.class);
		if (role != null) {
			Subject subject = getSubject();
			if (subject != null) {
				return subject.hasPermission(role.value());
			}
			return false;
		}
		return true;
	}

	public static Subject getSubject() {
		if (Frontend.isAvailable()) {
			PageBrowser pageBrowser = Frontend.getBrowser();
			return pageBrowser != null ? pageBrowser.getSubject() : null;
		} else {
			return Authorization.getInstance().getSubject();
		}
	}

}

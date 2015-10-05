package org.minimalj.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.page.PageBrowser;
import org.minimalj.transaction.Role;
import org.minimalj.transaction.Transaction;
import org.minimalj.transaction.Transaction.TransactionType;

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
		Role role = getRole(transaction);
		return hasPermission(role);
	}

	public static boolean hasPermission(Class<?> clazz, TransactionType transactionType) {
		Role role = findRoleByType(clazz, transactionType);
		return hasPermission(role);
	}

	private static boolean hasPermission(Role role) {
		if (role != null) {
			Subject subject = getSubject();
			if (subject != null) {
				return subject.hasPermission(role.value());
			}
			return false;
		}
		return true;
	}
	
	public static Role getRole(Transaction<?> transaction) {
		TransactionType transactionType = transaction.getType();
		boolean isPersistenceTransaction = transactionType != null;
		if (isPersistenceTransaction) {
			Role role = findRoleByType(transaction.getClass(), transactionType);
			if (role == null) {
				role = findRoleByType(transaction.getClazz(), transactionType);
			}
			return role;
		} else {
			return findRoleByType(transaction.getClass(), TransactionType.ALL);
		}
	}
	
	private static Role findRoleByType(Class<?> clazz, TransactionType type) {
		Role[] roles = clazz.getAnnotationsByType(Role.class);
		Role role = findRoleByType(roles, type);
		if (role != null) {
			return role;
		}
		roles = clazz.getPackage().getAnnotationsByType(Role.class);
		role = findRoleByType(roles, type);
		return role;
	}
	
	private static Role findRoleByType(Role[] roles, TransactionType type) {
		for (Role role : roles) {
			if (role.transaction() == type) {
				return role;
			}
		}
		for (Role role : roles) {
			if (role.transaction() == TransactionType.ALL) {
				return role;
			}
		}
		return null;
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

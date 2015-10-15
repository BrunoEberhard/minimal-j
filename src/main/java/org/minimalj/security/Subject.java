package org.minimalj.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.page.PageBrowser;
import org.minimalj.transaction.PersistenceTransaction;
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
		Role role = getRole(transaction);
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
		boolean isPersistenceTransaction = transaction instanceof PersistenceTransaction;
		if (isPersistenceTransaction) {
			PersistenceTransaction<?> persistenceTransaction = (PersistenceTransaction<?>) transaction;
			Role role = findRoleByType(transaction.getClass(), persistenceTransaction.getClass());
			if (role == null) {
				role = findRoleByType(persistenceTransaction.getEntityClazz(), persistenceTransaction.getClass());
			}
			return role;
		} else {
			return findRoleByType(transaction.getClass(), Transaction.class);
		}
	}
	
	private static Role findRoleByType(Class<?> clazz, Class<? extends Transaction> transactionClass) {
		Role[] roles = clazz.getAnnotationsByType(Role.class);
		Role role = findRoleByType(roles, transactionClass);
		if (role != null) {
			return role;
		}
		roles = clazz.getPackage().getAnnotationsByType(Role.class);
		role = findRoleByType(roles, transactionClass);
		return role;
	}
	
	private static Role findRoleByType(Role[] roles, Class<? extends Transaction> transactionClass) {
		for (Role role : roles) {
			if (role.transaction() == transactionClass) {
				return role;
			}
		}
		// TODO respect class hierachy when retrieving needed role for a transaction
		// the following lines only go by order of the roles not by the hierachy
//		for (Role role : roles) {
//			if (role.transactionClass().isAssignableFrom(transactionClass)) {
//				return role;
//			}
//		}
		
		// check for the Transaction.class as this is the default in the annotation
		for (Role role : roles) {
			if (role.transaction() == Transaction.class) {
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

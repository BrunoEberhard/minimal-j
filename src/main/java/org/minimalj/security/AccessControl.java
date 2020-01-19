package org.minimalj.security;

public interface AccessControl {

	boolean hasAccess(Subject subject);

}

package org.minimalj.security;

public interface RememberMeAuthentication {

	Subject remember(String rememberMeCookie);

}
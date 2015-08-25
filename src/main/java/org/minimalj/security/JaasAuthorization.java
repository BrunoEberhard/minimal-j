package org.minimalj.security;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

public class JaasAuthorization extends Authorization {

	private final MinimalCallbackHandler minimalCallbackHandler;
	private final LoginContext loginContext;

	public JaasAuthorization(String loginConfiguration) {
		try {
			this.minimalCallbackHandler = new MinimalCallbackHandler();
			this.loginContext = new LoginContext(loginConfiguration, minimalCallbackHandler);
		} catch (LoginException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected synchronized boolean checkLogin(UserPassword login) {
		minimalCallbackHandler.setUser(login.user);
		minimalCallbackHandler.setPassword(login.password);
		try {
			loginContext.login();
			return true;
		} catch (LoginException le) {
			return false;
		}
	}

	private class MinimalCallbackHandler implements CallbackHandler {

		private String user;
		private char[] password;

		public void setUser(String user) {
			this.user = user;
		}

		public void setPassword(char[] password) {
			this.password = password;
		}

		@Override
		public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
			for (Callback callback : callbacks) {
				if (callback instanceof NameCallback) {
					NameCallback nameCallback = (NameCallback) callback;
					nameCallback.setName(user);
				} else if (callback instanceof PasswordCallback) {
					PasswordCallback passwordCallback = (PasswordCallback) callback;
					passwordCallback.setPassword(password);
				} else {
					throw new UnsupportedCallbackException(callback);
				}
			}
		}
	}
}

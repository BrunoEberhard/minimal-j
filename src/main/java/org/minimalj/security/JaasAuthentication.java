package org.minimalj.security;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.minimalj.security.model.UserPassword;

// TODO...
public class JaasAuthentication extends Authentication {
	private static final long serialVersionUID = 1L;

	private final transient MinimalCallbackHandler minimalCallbackHandler;
	private final transient LoginContext loginContext;

	public JaasAuthentication(String jaasConfiguration) {
		try {
			this.minimalCallbackHandler = new MinimalCallbackHandler();
			this.loginContext = new LoginContext(jaasConfiguration, minimalCallbackHandler);
		} catch (LoginException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void login(LoginListener loginListener) {
		// TODO Auto-generated method stub
	}
	
//	@Override
	protected Subject login(UserPassword userPassword) {
		minimalCallbackHandler.setUser(userPassword.user);
		minimalCallbackHandler.setPassword(userPassword.password);
		try {
			loginContext.login();
			// TODO retrieve actual roles
		} catch (LoginException le) {
			return null;
		}
//		Subject subject = createSubject(userPassword.user);
//		return subject;
		return null;
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

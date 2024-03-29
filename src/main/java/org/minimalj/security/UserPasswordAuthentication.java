package org.minimalj.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Validator;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.CheckBoxFormElement;
import org.minimalj.frontend.form.element.PasswordFormElement;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.page.Page.Dialog;
import org.minimalj.model.Keys;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.repository.query.By;
import org.minimalj.security.model.RememberMeToken;
import org.minimalj.security.model.UserData;
import org.minimalj.security.model.UserPassword;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

public abstract class UserPasswordAuthentication extends Authentication implements RememberMeAuthentication {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(UserPasswordAuthentication.class.getName());
	
	@Override
	public Action getLoginAction() {
		return new UserPasswordLoginAction<>(new UserPassword(), createForm());
	}
	
	private Form<UserPassword> createForm() {
		Form<UserPassword> form = new Form<UserPassword>();
		form.line(UserPassword.$.user);
		form.line(new PasswordFormElement(UserPassword.$.password));
		if (REMEMBER_ME) {
			form.line(new CheckBoxFormElement(Keys.getProperty(UserPassword.$.rememberMe), Resources.getString("UserPassword.rememberMe"), true, false));
		}
		return form;
	}
	
	public static class UserPasswordLoginAction<T extends UserPassword> extends Action implements Dialog {
		private final T userPassword;
		private final Form<T> form;
		private LoginAction loginAction;
		
		public UserPasswordLoginAction(T userPassword, Form<T> form) {
			super(Resources.getString("Login.title"));
			this.userPassword = userPassword;
			this.form = form;
		}

		@Override
		public void run() {
			userPassword.rememberMe = REMEMBER_ME && Configuration.isDevModeActive();
			
			loginAction = new LoginAction();
			
			form.setChangeListener(this::validate);
			form.setObject(userPassword);
			
			Frontend.getInstance().showLogin(this);
		}
		
		@Override
		public String getTitle() {
			return Resources.getString("Login.title");
		}
		
		@Override
		public IContent getContent() {
			return form.getContent();
		}
		
		@Override
		public Action getSaveAction() {
			return loginAction;
		}
		
		private class CancelAction extends Action {
			@Override
			public void run() {
				Frontend.closeDialog(UserPasswordLoginAction.this);
			}
		}
		
		@Override
		public Action getCancelAction() {
			return new CancelAction();
		}

		private boolean validate(Form<?> form) {
			List<ValidationMessage> validationMessages = Validator.validate(userPassword);
			form.indicate(validationMessages);
			return validationMessages.isEmpty();
		}
		
		/**
		 * Allows additional application custom validation before login.
		 * Use showError or showInformation if something wrong.
		 */
		protected boolean valid(T userPassword) {
			return true;
		}

		protected final class LoginAction extends Action {
			
			@Override
			public void run() {
				if (valid(userPassword) && validate(form)) {
					Subject subject = Backend.execute(new LoginTransaction(userPassword));
					if (subject != null) {
						Frontend.getInstance().login(subject);
						Frontend.closeDialog(UserPasswordLoginAction.this);
					} else {
						Frontend.showMessage(Resources.getString("UsernamePasswordInvalid"));
					}
				}
			}
		}
	}
	
	public static class LoginTransaction implements Transaction<Subject> {
		private static final long serialVersionUID = 1L;
		
		private final UserPassword userPassword;
		
		public LoginTransaction(UserPassword userPassword) {
			Objects.requireNonNull(userPassword);
			Objects.requireNonNull(userPassword.user);
			
			this.userPassword = userPassword;
		}
		
		@Override
		public Subject execute() {
			String userToRetrieve = userPassword.getUserToRetrieve();
			UserData user = ((UserPasswordAuthentication) Backend.getInstance().getAuthentication()).retrieveUser(userToRetrieve, userPassword.password);
			if (user == null) {
				return null;
			}
			if (userPassword.rememberMe) {
				setRememberMeCookie(userToRetrieve, userPassword.password);
			}
			return Backend.getInstance().getAuthentication().createSubject(user);
		}
	}
	
	@Override
	protected void forgetMe() {
		super.forgetMe();
		if (REMEMBER_ME) {
			if (PERSISTENT_REMEMBER_ME) {
				Backend.delete(RememberMeToken.class, By.field(RememberMeToken.$.userName, Subject.getCurrent().getName()));
			} else {
				((JsonFrontend) Frontend.getInstance()).getPageManager().setRememberMeCookie(null);
			}
		}
	}
	
	protected UserData retrieveUser(String userName, char[] password) {
		UserData user = retrieveUser(userName);
		if (user != null && user.getPassword().validatePassword(password)) {
			return user;
		} else {
			return null;
		}
	}

	protected abstract UserData retrieveUser(String userName);

	// RememberMe

	protected static final boolean REMEMBER_ME = Frontend.isAvailable() && Frontend.getInstance() instanceof JsonFrontend
			&& ("true".equals(Configuration.get("MjRememberMe")) || Configuration.isDevModeActive() && !"false".equals(Configuration.get("MjRememberMe")));
	private static final boolean PERSISTENT_REMEMBER_ME = Arrays.stream(Application.getInstance().getEntityClasses()).anyMatch(c -> c == RememberMeToken.class);
	private static final SecureRandom random = new SecureRandom();

	protected static void setRememberMeCookie(String userName, char[] password) {
		if (PERSISTENT_REMEMBER_ME) {
			RememberMeToken rememberMeToken = new RememberMeToken();
			rememberMeToken.series = generateRandomString();
			rememberMeToken.token = generateRandomString();
			rememberMeToken.lastUsed = LocalDateTime.now();
			rememberMeToken.userName = userName;
			Backend.save(rememberMeToken);
			((JsonFrontend) Frontend.getInstance()).getPageManager().setRememberMeCookie(rememberMeToken.series + ":" + rememberMeToken.token);
		} else {
			UserData user = ((UserPasswordAuthentication) Backend.getInstance().getAuthentication()).retrieveUser(userName, password);
			String timestamp = String.valueOf(System.currentTimeMillis());
			String rememberMeCookie = user.getName() + ":" + timestamp + ":" + sign(user, timestamp);
			((JsonFrontend) Frontend.getInstance()).getPageManager().setRememberMeCookie(rememberMeCookie);
		}
	}

	private static String generateRandomString() {
		byte[] newSeries = new byte[RememberMeToken.TOKEN_SIZE];
		random.nextBytes(newSeries);
		return Base64.getEncoder().encodeToString(newSeries).substring(0, RememberMeToken.TOKEN_SIZE);
	}

	private static String sign(UserData user, String timestamp) {
		String key = getRememberMeKey();
		String passwordHash = user.getPassword().hash != null ? Base64.getEncoder().encodeToString(user.getPassword().hash) : "";
		String data = user.getName() + ":" + timestamp + ":" + passwordHash + ":" + key;
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			return Base64.getEncoder().encodeToString(digest.digest(data.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("No MD5 algorithm available");
		}
	}

	private static String getRememberMeKey() {
		String key = Configuration.get("MjRememberMeKey", "");
		if (StringUtils.isEmpty(key) && !Configuration.isDevModeActive()) {
			throw new IllegalStateException(
					"MjRememberMeKey must not be empty in production mode.\nThe next admin may forget to set the configuration and all previous token invalidations would be forgotten.");
		}
		return key;
	}

	/**
	 * Try to evaluate subject based on the provided rememberMeCookie
	 * 
	 * @param rememberMeCookie the String from the browser
	 * @return The subject or <code>null</code>
	 */
	@Override
	public Subject remember(String rememberMeCookie) {
		if (rememberMeCookie == null) {
			throw new IllegalStateException("rememberMeCookie is null");
		}
		if (PERSISTENT_REMEMBER_ME) {
			return rememberByPersistence(rememberMeCookie);
		} else {
			return rememberByToken(rememberMeCookie);
		}
	}
	
	private Subject rememberByPersistence(String rememberMeCookie) {
		int index = rememberMeCookie.indexOf(':');
		if (index <= 0 || index > rememberMeCookie.length() - 1) {
			throw new IllegalArgumentException("rememberMeCookie has invalid format: " + rememberMeCookie);
		}

		String series = rememberMeCookie.substring(0, index);
		String token = rememberMeCookie.substring(index + 1, rememberMeCookie.length());
		
		List<RememberMeToken> rememberMeTokens = Backend.getInstance().getRepository().find(RememberMeToken.class, By.field(RememberMeToken.$.series, series));
		if (!rememberMeTokens.isEmpty()) {
			RememberMeToken rememberMeToken = rememberMeTokens.get(0);
			if (!StringUtils.equals(token, rememberMeToken.token)) {
				logger.warning("Invalid rememberMeCookie: " + rememberMeCookie);
				return null;
			}

			UserData user = ((UserPasswordAuthentication) Backend.getInstance().getAuthentication()).retrieveUser(rememberMeToken.userName);
			if (user == null) {
				logger.warning("User not found for rememberMeCookie: " + rememberMeCookie);
				return null;
			}
			return Backend.getInstance().getAuthentication().createSubject(user);
		} else {
			return null;
		}
	}
	
	private Subject rememberByToken(String rememberMeCookie) {
		String[] parts = rememberMeCookie.split(":");
		if (parts.length != 3) {
			logger.warning("rememberMeCookie has invalid format (3 parts required): " + rememberMeCookie);
			return null;
		}

		UserData user = retrieveUser(parts[0]);
		if (user == null) {
			logger.warning("User not found for rememberMeCookie: " + rememberMeCookie);
			return null;
		}

		long timestamp = Long.valueOf(parts[1]);
		if (timestamp < System.currentTimeMillis() - 14 * 24 * 3600 * 1000) {
			logger.info("RememberMeCookie has expired: " + rememberMeCookie);
			return null;
		}

		String expectedSignature = sign(user, parts[1]);
		if (!StringUtils.equals(expectedSignature, parts[2])) {
			logger.severe("Invalid RememberMeCookie: " + rememberMeCookie);
			return null;
		}

		return Backend.getInstance().getAuthentication().createSubject(user);
	}

}
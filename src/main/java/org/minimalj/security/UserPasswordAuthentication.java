package org.minimalj.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Validator;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.CheckBoxFormElement;
import org.minimalj.frontend.form.element.PasswordFormElement;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.model.Keys;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.repository.query.By;
import org.minimalj.security.model.RememberMeToken;
import org.minimalj.security.model.User;
import org.minimalj.security.model.UserPassword;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

public abstract class UserPasswordAuthentication extends Authentication implements RememberMeAuthentication {
	private static final long serialVersionUID = 1L;

	@Override
	public Action getLoginAction() {
		return new UserPasswordLoginAction();
	}
	
	public static class UserPasswordLoginAction extends Action {
		private UserPassword userPassword;
		private Form<UserPassword> form;
		private LoginAction loginAction;
		private Optional<IDialog> dialog;
		
		public UserPasswordLoginAction() {
			super(Resources.getString("Login.title"));
		}

		@Override
		public void run() {
			userPassword = new UserPassword();
			userPassword.rememberMe = REMEMBER_ME && Configuration.isDevModeActive();
			
			form = createForm();
			
			loginAction = new LoginAction();
			
			form.setChangeListener(this::validate);
			form.setObject(userPassword);
			
			dialog = Frontend.getInstance().showLogin(form.getContent(), loginAction);
		}
		
		private boolean validate(Form<?> form) {
			List<ValidationMessage> validationMessages = Validator.validate(userPassword);
			form.indicate(validationMessages);
			return validationMessages.isEmpty();
		}

		protected Form<UserPassword> createForm() {
			Form<UserPassword> form = new Form<UserPassword>();
			form.line(UserPassword.$.user);
			form.line(new PasswordFormElement(UserPassword.$.password));
			if (REMEMBER_ME) {
				form.line(new CheckBoxFormElement(Keys.getProperty(UserPassword.$.rememberMe), Resources.getString("UserPassword.rememberMe"), true, false));
			}
			return form;
		}
		
		protected final class LoginAction extends Action {
			
			@Override
			public void run() {
				if (validate(form)) {
					Subject subject = save(userPassword);
					if (subject != null) {
						if (userPassword.rememberMe) {
							setRememberMeCookie(userPassword);
						}
						Frontend.getInstance().login(subject);
						dialog.ifPresent(IDialog::closeDialog);
					} else {
						Frontend.showMessage(Resources.getString("UsernamePasswordInvalid"));
					}
				}
			}
		}

		protected Subject save(UserPassword userPassword) {
			LoginTransaction loginTransaction = new LoginTransaction(userPassword);
			return Backend.execute(loginTransaction);
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
		
		public UserPassword getLogin() {
			return userPassword;
		}
		
		@Override
		public Subject execute() {
			User user = ((UserPasswordAuthentication) Backend.getInstance().getAuthentication()).retrieveUser(userPassword.user, userPassword.password);
			if (user == null) {
				return null;
			}
			return Backend.getInstance().getAuthentication().createSubject(userPassword.user, user.getRoleNames());
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
	
	protected User retrieveUser(String userName, char[] password) {
		User user = retrieveUser(userName);
		if (user != null && user.password.validatePassword(password)) {
			return user;
		} else {
			return null;
		}
	}

	protected abstract User retrieveUser(String userName);

	// RememberMe

	private static final boolean REMEMBER_ME = Frontend.isAvailable() && Frontend.getInstance() instanceof JsonFrontend
			&& ("true".equals(Configuration.get("MjRememberMe")) || Configuration.isDevModeActive());
	private static final boolean PERSISTENT_REMEMBER_ME = Arrays.stream(Application.getInstance().getEntityClasses()).anyMatch(c -> c == RememberMeToken.class);
	private static final SecureRandom random = new SecureRandom();

	private static void setRememberMeCookie(UserPassword userPassword) {
		if (PERSISTENT_REMEMBER_ME) {
			RememberMeToken rememberMeToken = new RememberMeToken();
			rememberMeToken.series = generateRandomString();
			rememberMeToken.token = generateRandomString();
			rememberMeToken.lastUsed = LocalDateTime.now();
			rememberMeToken.userName = userPassword.user;
			Backend.save(rememberMeToken);
			((JsonFrontend) Frontend.getInstance()).getPageManager().setRememberMeCookie(rememberMeToken.series + ":" + rememberMeToken.token);
		} else {
			User user = ((UserPasswordAuthentication) Backend.getInstance().getAuthentication()).retrieveUser(userPassword.user, userPassword.password);
			String timestamp = String.valueOf(System.currentTimeMillis());
			String rememberMeCookie = user.name + ":" + timestamp + ":" + sign(user, timestamp);
			((JsonFrontend) Frontend.getInstance()).getPageManager().setRememberMeCookie(rememberMeCookie);
		}
	}

	private static String generateRandomString() {
		byte[] newSeries = new byte[RememberMeToken.TOKEN_SIZE];
		random.nextBytes(newSeries);
		return Base64.getEncoder().encodeToString(newSeries).substring(0, RememberMeToken.TOKEN_SIZE);
	}

	private static String sign(User user, String timestamp) {
		String key = getRememberMeKey();
		String passwordHash = user.password.hash != null ? Base64.getEncoder().encodeToString(user.password.hash) : "";
		String data = user.name + ":" + timestamp + ":" + passwordHash + ":" + key;
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
		RememberTransaction rememberTransaction = new RememberTransaction(rememberMeCookie);
		return Backend.execute(rememberTransaction);
	}

	// https://stackoverflow.com/questions/244882/what-is-the-best-way-to-implement-remember-me-for-a-website
	public static class RememberTransaction implements Transaction<Subject> {
		private static final Logger logger = Logger.getLogger(RememberTransaction.class.getName());
		private static final long serialVersionUID = 1L;
	
		private final String rememberMeCookie;
	
		public RememberTransaction(String rememberMeCookie) {
			this.rememberMeCookie = Objects.requireNonNull(rememberMeCookie);
		}
	
		@Override
		public Subject execute() {
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
			
			List<RememberMeToken> rememberMeTokens = Backend.find(RememberMeToken.class, By.field(RememberMeToken.$.series, series));
			if (!rememberMeTokens.isEmpty()) {
				RememberMeToken rememberMeToken = rememberMeTokens.get(0);
				if (!StringUtils.equals(token, rememberMeToken.token)) {
					logger.warning("Invalid rememberMeCookie: " + rememberMeCookie);
					return null;
				}
	
				User user = ((UserPasswordAuthentication) Backend.getInstance().getAuthentication()).retrieveUser(rememberMeToken.userName);
				if (user == null) {
					logger.warning("User not found for rememberMeCookie: " + rememberMeCookie);
					return null;
				}
				return Backend.getInstance().getAuthentication().createSubject(rememberMeToken.userName, user.getRoleNames());
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
	
			User user = ((UserPasswordAuthentication) Backend.getInstance().getAuthentication()).retrieveUser(parts[0]);
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
	
			return Backend.getInstance().getAuthentication().createSubject(user.name, user.getRoleNames());
		}


	}
}
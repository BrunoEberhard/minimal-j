package org.minimalj.security;

import java.io.IOException;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.minimalj.security.model.User;
import org.minimalj.security.model.UserRole;

public class TextFileAuthentication extends UserPasswordAuthentication {
	private static final long serialVersionUID = 1L;

	private final transient Map<String, User> userByName = new HashMap<>();

	public TextFileAuthentication(String loginConfiguration) {
		super();
		try {
			loadUsers(loginConfiguration);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void loadUsers(String userTextFile) throws IOException {
		Properties p = new Properties();
		p.load(getClass().getClassLoader().getResourceAsStream(userTextFile));

		Enumeration<?> e = p.propertyNames();
		while (e.hasMoreElements()) {
			String name = e.nextElement().toString();

			String passwordAndRoles = p.getProperty(name);
			String[] split = passwordAndRoles.split(",");

			User user = new User();
			user.password.hash = Base64.getDecoder().decode(split[0].trim());
			user.password.salt = Base64.getDecoder().decode(split[1].trim());
			
			for (int i = 2; i<split.length; i++) {
				user.roles.add(new UserRole(split[i].trim()));
			}
			userByName.put(name, user);
		}
	}
	
	@Override
	protected User retrieveUser(String userName) {
		return userByName.get(userName);
	}

	/**
	 * Can be used to create entries in the users textfile
	 * 
	 * @param args username and password
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Two arguments needed as username and password");
			System.exit(-1);
		}
		User user = new User();
		user.name = args[0];
		user.password.setPassword(args[1].toCharArray());
		for (int i = 2; i<args.length; i++) {
			user.roles.add(new UserRole(args[i].trim()));
		}
		System.out.println(user.format());
	}
	
}

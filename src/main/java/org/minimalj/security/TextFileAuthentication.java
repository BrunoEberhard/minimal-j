package org.minimalj.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.minimalj.security.model.User;
import org.minimalj.security.model.UserData;
import org.minimalj.security.model.UserRole;

public class TextFileAuthentication extends UserPasswordAuthentication {
	private static final long serialVersionUID = 1L;

	private final transient Map<String, UserData> userByName = new HashMap<>();

	public TextFileAuthentication(String userTextFile) {
		super();
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(userTextFile)) {
			loadUsers(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected TextFileAuthentication() {
	}
	
	protected void loadUsers(InputStream is) throws IOException {
		Properties p = new Properties();
		p.load(is);

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
	protected UserData retrieveUser(String userName) {
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

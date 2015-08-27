package org.minimalj.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PropertiesAuthorization extends Authorization {

	private Map<String, Subject> userByName = new HashMap<>();
	private Map<String, String> passwordByName = new HashMap<>();

	public PropertiesAuthorization(String loginConfiguration) {
		try {
			loadUsers(loginConfiguration);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void loadUsers(String loginConfiguration) throws IOException {
		Properties p = new Properties();
		p.load(getClass().getClassLoader().getResourceAsStream(loginConfiguration));

		Enumeration<?> e = p.propertyNames();
		while (e.hasMoreElements()) {
			String name = e.nextElement().toString();

			String passwordAndRoles = p.getProperty(name);
			String[] split = passwordAndRoles.split(",");

			passwordByName.put(name, split[0]);

			Subject user = new Subject();
			user.setName(name);
			for (int i = 1; i<split.length; i++) {
				user.getRoles().add(split[i].trim());
			}
			userByName.put(name, user);
		}
	}

	@Override
	protected List<String> retrieveRoles(UserPassword login) {
		if (passwordByName.containsKey(login.user)) {
			char[] p = passwordByName.get(login.user).toCharArray();
			if (Arrays.equals(p, login.password)) {
				return userByName.get(login.user).getRoles();
			}
		}
		return null;
	}

}

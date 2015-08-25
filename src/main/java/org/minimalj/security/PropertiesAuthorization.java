package org.minimalj.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
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

			Subject user = new Subject();
			user.setName(name);
			if (split.length > 1) {
				user.setRoles(Arrays.asList(split).subList(1, split.length));
			}
			userByName.put(name, user);

			passwordByName.put(name, split[0]);
		}
	}

	@Override
	protected boolean checkLogin(UserPassword login) {
		if (passwordByName.containsKey(login.user)) {
			char[] p = passwordByName.get(login.user).toCharArray();
			return Arrays.equals(p, login.password);
		} else {
			return false;
		}
	}

}

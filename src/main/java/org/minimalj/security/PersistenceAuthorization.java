package org.minimalj.security;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.sql.DataSource;

import org.minimalj.backend.Persistence;
import org.minimalj.backend.sql.SqlPersistence;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.transaction.criteria.By;

public class PersistenceAuthorization extends Authorization {

	private final Persistence authorizationPersistence;
	
	public PersistenceAuthorization(Persistence authorizationPersistence) {
		this.authorizationPersistence = authorizationPersistence;
	}
	
	public PersistenceAuthorization(DataSource dataSource) {
		this.authorizationPersistence = new SqlPersistence(dataSource, User.class);
	}

	@Override
	protected List<String> retrieveRoles(UserPassword userPassword) {
		List<User> userList = authorizationPersistence.read(User.class, By.field(User.$.name, userPassword.user), 1);
		if (userList.isEmpty()) {
			return null;
		}
		User user = userList.get(0);
		if (!user.validatePassword(userPassword.password)) {
			throw new LoginFailedException();
		}
		List<String> roleNames = user.roles.stream().map((role) -> role.name).collect(Collectors.toList());
		return roleNames;
	}

	public static class User implements Serializable {
		private static final long serialVersionUID = 1L;
		public static final User $ = Keys.of(User.class);
		
		public static final String HASH_ALGORITHM = "PBKDF2WithHmacSHA1";
		public static final int HASH_ITERATIONS = 1000;
		public static final int HASH_SIZE = 24;
		public static final int SALT_SIZE = 24;
		
		@Size(255)
		public String name;
		
		@Size(HASH_SIZE)
		public byte[] hash;

		@Size(SALT_SIZE)
		public byte[] salt;
		
		List<UserRole> roles = new ArrayList<>();
		
		public void setPassword(char[] password) {
			salt = new byte[SALT_SIZE];
			SecureRandom random = new SecureRandom();
			random.nextBytes(salt);
			hash = hash(password, salt);
		}

		public boolean validatePassword(char[] password) {
			byte[] hashToValidate = hash(password, salt);
			SecureRandom random = new SecureRandom();
			try {
				Thread.sleep(random.nextInt(100));
			} catch (InterruptedException e) {
				throw new RuntimeException("Password cannot be validated", e);
			}
			return Arrays.equals(hashToValidate, hash);
		}

		private byte[] hash(char[] password, byte[] salt) {
			PBEKeySpec keySpec = new PBEKeySpec(password, salt, HASH_ITERATIONS, HASH_SIZE * 8);
			try {
				SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(HASH_ALGORITHM);
				return secretKeyFactory.generateSecret(keySpec).getEncoded();
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				throw new RuntimeException("Password cannot be stored", e);
			}
		}
	}

	public static class UserRole implements Serializable {
		private static final long serialVersionUID = 1L;
		
		@Size(255)
		public String name;
	}
}

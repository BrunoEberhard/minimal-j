package org.minimalj.security;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class Password implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final Password $ = Keys.of(Password.class);
	
	public static final String HASH_ALGORITHM = "PBKDF2WithHmacSHA1";
	public static final int HASH_ITERATIONS = 1000;
	public static final int HASH_SIZE = 24;
	public static final int SALT_SIZE = 24;
	
	@Size(HASH_SIZE)
	public byte[] hash;

	@Size(SALT_SIZE)
	public byte[] salt;
	
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
			throw new RuntimeException("Hashing not possible", e);
		}
	}
}
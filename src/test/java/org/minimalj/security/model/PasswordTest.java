package org.minimalj.security.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.junit.Test;
import org.minimalj.application.Configuration;

public class PasswordTest {
	private static final char[] SECRET = "correct horse".toCharArray();

	/** A password as it was stored before version 2: PBKDF2WithHmacSHA1, 1000 iterations, 24 bytes salt */
	private static Password legacyPassword(char[] password) throws Exception {
		Password legacy = new Password();
		legacy.salt = new byte[24];
		new SecureRandom().nextBytes(legacy.salt);
		PBEKeySpec keySpec = new PBEKeySpec(password, legacy.salt, 1000, 24 * 8);
		legacy.hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(keySpec).getEncoded();
		return legacy;
	}

	@Test
	public void newPasswordUsesVersion2() {
		Password password = new Password();
		password.setPassword(SECRET);

		assertEquals(21, password.salt.length);
		assertEquals(2, password.salt[0]);
		assertEquals(Password.getHashIterations(), ByteBuffer.wrap(password.salt, 1, 4).getInt());
		assertEquals(Password.HASH_SIZE, password.hash.length);
		assertTrue(password.salt.length <= Password.SALT_SIZE);
		assertTrue(password.validatePassword(SECRET));
		assertFalse(password.validatePassword("wrong".toCharArray()));
		assertFalse(password.needsRehash());
	}

	@Test
	public void legacyPasswordStillValidates() throws Exception {
		Password legacy = legacyPassword(SECRET);

		assertTrue(legacy.validatePassword(SECRET));
		assertFalse(legacy.validatePassword("wrong".toCharArray()));
		assertTrue(legacy.needsRehash());

		legacy.setPassword(SECRET);
		assertTrue(legacy.validatePassword(SECRET));
		assertFalse(legacy.needsRehash());
	}

	@Test
	public void saltsDiffer() {
		Password a = new Password();
		a.setPassword(SECRET);
		Password b = new Password();
		b.setPassword(SECRET);
		assertFalse(java.util.Arrays.equals(a.hash, b.hash));
	}

	@Test
	public void fewerIterationsNeedRehash() {
		Password password = new Password();
		password.setPassword(SECRET);
		byte[] salt = password.salt.clone();
		ByteBuffer.wrap(salt).putInt(1, 1000);
		password.salt = salt;
		assertTrue(password.needsRehash());
	}

	@Test
	public void weakEncryption() {
		Configuration.set(Password.WEAK_ENCRYPTION_CONFIGURATION, "true");
		Password password = new Password();
		try {
			password.setPassword(SECRET);
			assertEquals(Password.WEAK_HASH_ITERATIONS, ByteBuffer.wrap(password.salt, 1, 4).getInt());
			assertTrue(password.validatePassword(SECRET));
			assertFalse(password.needsRehash());
		} finally {
			Configuration.set(Password.WEAK_ENCRYPTION_CONFIGURATION, "");
		}
		// Without the configuration the weak hash stays valid but should be replaced
		assertTrue(password.validatePassword(SECRET));
		assertTrue(password.needsRehash());
	}

	@Test
	public void unsetOrUnknownFormatDoesNotValidate() {
		Password empty = new Password();
		assertFalse(empty.validatePassword(SECRET));
		assertFalse(empty.needsRehash());

		Password unknown = new Password();
		unknown.setPassword(SECRET);
		unknown.salt[0] = 3;
		assertFalse(unknown.validatePassword(SECRET));
		assertFalse(unknown.needsRehash());
	}

	@Test
	public void hashKeepsStoredValue() {
		Password password = new Password();
		password.setPassword(SECRET);
		byte[] hash = password.hash.clone();
		password.validatePassword(SECRET);
		assertArrayEquals(hash, password.hash);
	}
}

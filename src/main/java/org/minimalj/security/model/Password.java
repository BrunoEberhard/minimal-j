package org.minimalj.security.model;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.minimalj.application.Configuration;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

/**
 * A hashed password. New passwords are hashed with PBKDF2WithHmacSHA256 and {@link #HASH_ITERATIONS} iterations.
 * For tests the configuration <code>MjWeakPasswordEncryption=true</code> makes hashing and validation fast, see
 * {@link #WEAK_ENCRYPTION_CONFIGURATION}.
 * Passwords hashed by older versions (PBKDF2WithHmacSHA1, 1000 iterations) can still be validated. Use
 * {@link #needsRehash()} after a successful validation to replace them.
 * <p>
 * The format is stored in the salt, so no additional column is needed and existing tables and text files stay
 * valid:
 * <ul>
 * <li>legacy: 24 random bytes</li>
 * <li>version 2: one byte version (2), four bytes iterations (big endian), 16 random bytes</li>
 * </ul>
 */
public class Password implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final Password $ = Keys.of(Password.class);

	public static final String HASH_ALGORITHM = "PBKDF2WithHmacSHA256";
	/** Iterations for new hashes, OWASP recommendation for PBKDF2-HMAC-SHA256 (2023) */
	public static final int HASH_ITERATIONS = 600_000;
	/**
	 * Configuration for tests only, never in production: with <code>MjWeakPasswordEncryption=true</code> new hashes
	 * use only {@link #WEAK_HASH_ITERATIONS} iterations and validation skips the random delay. As the iterations are
	 * stored with each hash, passwords hashed this way stay valid without the configuration, and
	 * {@link #needsRehash()} reports them.
	 */
	public static final String WEAK_ENCRYPTION_CONFIGURATION = "MjWeakPasswordEncryption";
	public static final int WEAK_HASH_ITERATIONS = 10;
	public static final int HASH_SIZE = 24;
	public static final int SALT_SIZE = 24;

	private static final String LEGACY_HASH_ALGORITHM = "PBKDF2WithHmacSHA1";
	private static final int LEGACY_HASH_ITERATIONS = 1000;
	private static final int LEGACY_SALT_SIZE = 24;

	private static final byte VERSION = 2;
	private static final int RANDOM_SIZE = 16;
	private static final int HEADER_SIZE = 1 + Integer.BYTES;

	private static final SecureRandom random = new SecureRandom();
	private static final Logger logger = Logger.getLogger(Password.class.getName());
	private static final AtomicBoolean weakEncryptionLogged = new AtomicBoolean();

	@Size(HASH_SIZE)
	public byte[] hash;

	@Size(SALT_SIZE)
	public byte[] salt;

	public void setPassword(char[] password) {
		byte[] randomPart = new byte[RANDOM_SIZE];
		random.nextBytes(randomPart);
		set(password, randomPart);
	}

	/**
	 * Set the password with no salt. This is useful in development mode. After a
	 * restart the remember me does still work and you don't have to login.
	 *
	 * @param password the new password
	 * @throws IllegalStateException if development mode is not active
	 */
	public void setPasswordWithoutSalt(char[] password) {
		if (!Configuration.isDevModeActive()) {
			throw new IllegalStateException("Passwords without salt are only allowed in develop mode");
		}
		set(password, new byte[RANDOM_SIZE]);
	}

	private void set(char[] password, byte[] randomPart) {
		int iterations = getHashIterations();
		salt = ByteBuffer.allocate(HEADER_SIZE + RANDOM_SIZE).put(VERSION).putInt(iterations).put(randomPart).array();
		hash = hash(password, salt, HASH_ALGORITHM, iterations);
	}

	/**
	 * @return true if the configuration <code>MjWeakPasswordEncryption</code> is set to true. Logs a warning the
	 *         first time.
	 */
	public static boolean isWeakEncryption() {
		String value = Configuration.get(WEAK_ENCRYPTION_CONFIGURATION);
		boolean weak = value != null && "true".equalsIgnoreCase(value.trim());
		if (weak && weakEncryptionLogged.compareAndSet(false, true)) {
			logger.warning(WEAK_ENCRYPTION_CONFIGURATION + " is active. Passwords are hashed weakly. Use this only for tests, never in production.");
		}
		return weak;
	}

	/**
	 * @return the iterations for new hashes: {@link #HASH_ITERATIONS}, or {@link #WEAK_HASH_ITERATIONS} with
	 *         <code>MjWeakPasswordEncryption=true</code>
	 */
	public static int getHashIterations() {
		return isWeakEncryption() ? WEAK_HASH_ITERATIONS : HASH_ITERATIONS;
	}

	/**
	 * This getter doesn't return the password. The password is encrypted in the
	 * setter method. This getter is only useful for creating a user form:<br>
	 * <pre>
	 * form.line(new PasswordFormElement(User.$.password));
	 * </pre>
	 * The length of the password is set to 32. This is arbitrary. Because the
	 * password is hashed anyway every length could be accepted.
	 *
	 * @return <code>null</code>
	 */
	@Size(32)
	public char[] getPassword() {
		if (Keys.isKeyObject(this)) {
			return Keys.methodOf(this, "password");
		} else {
			return null;
		}
	}

	/**
	 * @param password the password to check
	 * @return true if the password matches. false if not, if no password is set or if the format is unknown.
	 */
	public boolean validatePassword(char[] password) {
		byte[] hashToValidate = hash != null && salt != null ? hash(password, salt) : null;
		if (!isWeakEncryption()) {
			try {
				Thread.sleep(random.nextInt(100));
			} catch (InterruptedException e) {
				throw new RuntimeException("Password cannot be validated", e);
			}
		}
		return hashToValidate != null && MessageDigest.isEqual(hashToValidate, hash);
	}

	/**
	 * @return true if the password is set and was hashed with the legacy algorithm or with fewer iterations than
	 *         {@link #getHashIterations()}. After a successful {@link #validatePassword(char[])} the caller should then
	 *         call {@link #setPassword(char[])} with the same password and save the owner.
	 */
	public boolean needsRehash() {
		if (hash == null || salt == null) {
			return false;
		} else if (isLegacy(salt)) {
			return true;
		} else {
			return isVersion2(salt) && iterations(salt) < getHashIterations();
		}
	}

	private static boolean isLegacy(byte[] salt) {
		return salt.length == LEGACY_SALT_SIZE;
	}

	private static boolean isVersion2(byte[] salt) {
		return salt.length == HEADER_SIZE + RANDOM_SIZE && salt[0] == VERSION;
	}

	private static int iterations(byte[] salt) {
		return ByteBuffer.wrap(salt, 1, Integer.BYTES).getInt();
	}

	private static byte[] hash(char[] password, byte[] salt) {
		if (isLegacy(salt)) {
			return hash(password, salt, LEGACY_HASH_ALGORITHM, LEGACY_HASH_ITERATIONS);
		} else if (isVersion2(salt) && iterations(salt) > 0) {
			return hash(password, salt, HASH_ALGORITHM, iterations(salt));
		} else {
			// Unknown format, e.g. from a newer version. Validation fails.
			return null;
		}
	}

	private static byte[] hash(char[] password, byte[] salt, String algorithm, int iterations) {
		PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterations, HASH_SIZE * 8);
		try {
			SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(algorithm);
			return secretKeyFactory.generateSecret(keySpec).getEncoded();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new RuntimeException("Hashing not possible", e);
		} finally {
			keySpec.clearPassword();
		}
	}

	private static final char[] CHARS = "ABCDEFGHIJKLMNPQRSTUVWXabcdefghijkmnopqrstuvwx23456789!@#$%^&*()_+-{}".toCharArray();

	public static char[] generatePassword(int length) {
		char[] s = new char[length];
        Random random = new SecureRandom();

        for (int i = 0; i < length; i++) {
            s[i] = CHARS[random.nextInt(CHARS.length)];
        }
        return s;
	}
}

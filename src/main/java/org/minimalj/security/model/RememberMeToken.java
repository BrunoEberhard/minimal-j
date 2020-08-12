package org.minimalj.security.model;

import java.time.LocalDateTime;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

/**
 * Used to remember the last token for "Remember Me"
 * 
 */
public class RememberMeToken {
	public static final RememberMeToken $ = Keys.of(RememberMeToken.class);
	public static final int TOKEN_SIZE = 24;

	public Object id;

	@Size(255)
	public String userName;

	@Size(TOKEN_SIZE)
	public String token;

	@Size(TOKEN_SIZE)
	public String series;

	@Size(Size.TIME_WITH_MILLIS)
	public LocalDateTime lastUsed;
}

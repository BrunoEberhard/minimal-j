package org.minimalj.example.library.model;

import java.time.LocalDate;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class Cancellation {
	public static final Cancellation $ = Keys.of(Cancellation.class);

	@Size(255)
	public String reason;
	
	public LocalDate dateOfCanellation;
}

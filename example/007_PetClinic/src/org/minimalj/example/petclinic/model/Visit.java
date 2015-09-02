package org.minimalj.example.petclinic.model;

import java.time.LocalDate;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class Visit {
	public static final Visit $ = Keys.of(Visit.class);

    public LocalDate visitDate;

    @Size(255)
    public String description;

}

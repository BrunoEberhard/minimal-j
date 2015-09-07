package org.minimalj.example.petclinic.model;

import java.time.LocalDate;

import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.annotation.Size;
import org.minimalj.util.DateUtils;

public class Visit implements Rendering {
	public static final Visit $ = Keys.of(Visit.class);

    public LocalDate visitDate;

    @Size(255)
    public String description;
    
    @Override
    public String render(RenderType renderType) {
    	return DateUtils.format(visitDate) + ": " + description;
    }

}

package org.minimalj.example.petclinic.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Reference;
import org.minimalj.model.annotation.Size;

public class Pet {
	public static final Pet $ = Keys.of(Pet.class);
			
	public Object id;
	
	@NotEmpty @Size(30)
	public String name;
	
	@NotEmpty @Reference
	public Owner owner;
	
    public LocalDate birthDate;

    public PetType type;
    
    public final List<Visit> visits = new ArrayList<>();

}

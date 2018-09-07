package org.minimalj.example.minimalclinic.model;

import java.time.LocalDate;

import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Size;
import org.minimalj.util.DateUtils;

public class Pet implements Rendering {
	public static final Pet $ = Keys.of(Pet.class);
			
	public Object id;
	
	public Owner owner;
	
	@NotEmpty @Size(30)
	public String name;
	
	@NotEmpty
    public LocalDate birthDate;

	@NotEmpty
	public PetType type;
    
    @Override
    public String render() {
    	return name + ", " + DateUtils.format(birthDate) + ", " + type.render();
    }

}

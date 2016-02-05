package org.minimalj.example.petclinic.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Size;
import org.minimalj.util.DateUtils;

public class Pet implements Rendering {
	public static final Pet $ = Keys.of(Pet.class);
			
	public Object id;
	
	@NotEmpty @Size(30)
	public String name;
	
	@NotEmpty
	public Owner owner;
	
	@NotEmpty
    public LocalDate birthDate;

	@NotEmpty
	public PetType type;
    
    public final List<Visit> visits = new ArrayList<>();
        
    @Override
    public String render(RenderType renderType) {
    	return name + ", " + DateUtils.format(birthDate) + ", " + type.render(renderType);
    }

}

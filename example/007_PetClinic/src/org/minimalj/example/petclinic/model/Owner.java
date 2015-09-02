package org.minimalj.example.petclinic.model;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Size;

public class Owner {
	public static final Owner $ = Keys.of(Owner.class);
	
	public Object id;
	
	public final Person person = new Person();
	
    @NotEmpty @Size(255)
    public String address;

    @NotEmpty @Size(80)
    public String city;

    @NotEmpty @Size(20)
    // @Digits(fraction = 0, integer = 10)
    public String telephone;

}
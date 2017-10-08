package org.minimalj.repository.ignite;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class A {

	public static final A $ = Keys.of(A.class);
	
	public A() {
		// needed for reflection constructor
	}
	
	public A(String aName) {
		this.aName = aName;
	}
	
	public Object id;
	public int version;

	@Size(30)
	public String aName;
	public final List<B> b = new ArrayList<B>();
	public final List<C> c = new ArrayList<C>();
	public E e;
	
	@Size(5)
	public Integer int1;
	
	@Size(15)
	public Long long1;
}

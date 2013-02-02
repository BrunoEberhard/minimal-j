package ch.openech.test.db;

import java.util.ArrayList;
import java.util.List;

import ch.openech.mj.model.annotation.Size;

public class A {

	public A() {
		// needed for reflection constructor
	}
	
	public A(String aName) {
		this.aName = aName;
	}
	
	@Size(30)
	public String aName;
	public final List<B> b = new ArrayList<B>();
	public final List<C> c = new ArrayList<C>();
	public E e;
	
}

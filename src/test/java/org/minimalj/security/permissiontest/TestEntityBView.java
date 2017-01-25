package org.minimalj.security.permissiontest;

import org.minimalj.model.View;
import org.minimalj.transaction.Role;

@Role("RoleA")
public class TestEntityBView implements View<TestEntityB> {

	public Object id;

}

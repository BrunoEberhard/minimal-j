package org.minimalj.security.permissiontest;

import org.minimalj.model.Grant;
import org.minimalj.model.Grant.Privilege;

@Grant(value = "ReadRole")
@Grant(privilege = Privilege.UPDATE, value = "UpdateRole")
public class C {

	public Object id;

}

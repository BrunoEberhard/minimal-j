package org.minimalj.security.permissiontest;

import org.minimalj.model.annotation.Grant;
import org.minimalj.model.annotation.Grant.Privilege;

@Grant(value = "ReadRole")
@Grant(privilege = Privilege.UPDATE, value = "UpdateRole")
public class C {

	public Object id;

}

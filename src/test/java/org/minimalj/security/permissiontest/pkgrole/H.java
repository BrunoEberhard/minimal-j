package org.minimalj.security.permissiontest.pkgrole;

import org.minimalj.model.Grant;
import org.minimalj.model.Grant.Privilege;

@Grant(privilege = Privilege.UPDATE, value = "UpdateClassRole")
public class H {

	public Object id;

}

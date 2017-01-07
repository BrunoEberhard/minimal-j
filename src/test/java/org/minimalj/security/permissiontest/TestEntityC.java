package org.minimalj.security.permissiontest;

import org.minimalj.model.annotation.Grant;
import org.minimalj.model.annotation.Grant.Privilege;

@Grant("ReadRole")
@Grant(privilege = Privilege.UPDATE, value = "UpdateRole")
public class TestEntityC {

	public Object id;

}

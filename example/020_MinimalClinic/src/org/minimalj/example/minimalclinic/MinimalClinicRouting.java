package org.minimalj.example.minimalclinic;

import org.minimalj.example.minimalclinic.frontend.VetPage;
import org.minimalj.example.minimalclinic.frontend.VetTablePage;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.Routing;

public class MinimalClinicRouting extends Routing {

	@Override
	protected String getRoute(Page page) {
		if (page instanceof VetPage) {
			return "/vet/" + ((VetPage) page).getObjectId();
		} else if (page instanceof VetTablePage) {
			return "/vets";
		} else {
			return null;
		}
	}

	@Override
	protected Page createPage(String route) {
		if (route.startsWith("/vet/")) {
			return new VetPage(route.substring(5));
		} else if (route.equals("/vets")) {
			return new VetTablePage();
		} else {
			return null;
		}
	}

}

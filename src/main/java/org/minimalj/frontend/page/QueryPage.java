package org.minimalj.frontend.page;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.util.resources.Resources;

public class QueryPage implements Page {

	@Override
	public IContent getContent() {
		return Frontend.getInstance().createQueryContent();
	}
	
	@Override
	public String getTitle() {
		if (Resources.isAvailable(QueryPage.class.getSimpleName())) {
			return Resources.getString(QueryPage.class.getSimpleName(), Resources.OPTIONAL);
		} else {
			return Resources.getString("Application.name");
		}
	}

}

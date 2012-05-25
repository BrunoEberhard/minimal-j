package ch.openech.mj.page;

import ch.openech.mj.toolkit.ClientToolkit;

public class PageContextHelper {

	public static PageContext findContext(Object source) {
		Object object = source;
		while (!(object instanceof PageContext)) {
			object = ClientToolkit.getToolkit().getParent(object);
			if (object == null) {
				throw new IllegalArgumentException("PageContext not found");
			}
		}
		return (PageContext) object;
	}
}

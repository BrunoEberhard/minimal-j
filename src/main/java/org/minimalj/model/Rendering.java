package org.minimalj.model;

public interface Rendering {

	public static enum RenderType {
		PLAIN_TEXT, HMTL;
	}
	
	/*
	 * Note: If asked for HTML the answer can be a plain text. 
	 */
	public String render(RenderType renderType);
	
	public default String renderTooltip(RenderType renderType) {
		return null;
	}
	
	public default RenderType getPreferredRenderType(RenderType firstType, RenderType... otherTypes) {
		return firstType;
	}
	
	public static String render(Object o, RenderType renderType) {
		if (o instanceof Rendering) {
			return ((Rendering) o).render(renderType);
		} else if (o != null) {
			return o.toString();
		} else {
			return null;
		}
	}
	
}

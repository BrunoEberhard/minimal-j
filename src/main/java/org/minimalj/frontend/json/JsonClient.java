package org.minimalj.frontend.json;


public interface JsonClient {

	public void send(String json);
	
	public void setListener(JsonClientListener listener);
	
	public interface JsonClientListener {
		
		public void received(String json);
	}
	
}

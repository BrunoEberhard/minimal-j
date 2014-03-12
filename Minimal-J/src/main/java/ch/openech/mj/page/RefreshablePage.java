package ch.openech.mj.page;

public interface RefreshablePage extends Page {

	// Refresh wird grundsätzlich ansynchron ausgeführt und kann lange dauern
	public void refresh();
	
}

package ch.openech.mj.toolkit;

public interface VisualDialog {

	public void setResizable(boolean resizable);

	public void setCloseListener(CloseListener closeListener);

	public void setVisible(boolean visible);

	public interface CloseListener {
		
		public boolean close();
	}
	
}

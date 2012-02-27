package ch.openech.mj.toolkit;

public interface VisualDialog {

	public void setResizable(boolean resizable);

	public void setTitle(String title);
	
	public void setCloseListener(CloseListener closeListener);

	public void openDialog();
	
	public void closeDialog();

	public interface CloseListener {
		
		public boolean close();
	}
	
}

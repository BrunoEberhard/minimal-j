package ch.openech.mj.toolkit;


public interface IDialog {

	public void setCloseListener(CloseListener closeListener);

	public interface CloseListener {
		
		public boolean close();
	}

	void openDialog();

	void closeDialog();
	
}

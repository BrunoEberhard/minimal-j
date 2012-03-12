package ch.openech.mj.toolkit;

public interface ConfirmDialogListener {
	/**
	 * 
	 * @param result as in JOptionPane (be aware of -1 for CLOSED_OPTION)
	 */
	void onClose(int result);
}
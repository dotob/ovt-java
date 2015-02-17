package ui;

import javax.swing.JFrame;

public interface IMainWindow {
	void setStatusText(String txt);

	JFrame getFrame();

	void setWaitCursor();

	void setDefaultCursor();

	void startStatusProgress();

	void stopStatusProgress();
}

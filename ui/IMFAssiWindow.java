package ui;

import tools.ContactBundle;

public interface IMFAssiWindow extends IMainWindow {
	void startInfoPanelUpdate();

	void disableDataInputPanel();

	ContactBundle getCB();

	void sendData();

}

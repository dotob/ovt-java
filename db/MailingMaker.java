package db;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import tools.ContactBundle;
import ui.IMainWindow;

public class MailingMaker extends SwingWorker<Long, Object> {

	private ProgressMonitor pm;
	private ContactBundle allCB;
	private String mailingName;
	private IMainWindow parentWindow;

	public MailingMaker(ContactBundle allCBi, String mailingNamei, IMainWindow pw) {
		this.parentWindow = pw;
		this.allCB = allCBi;
		this.mailingName = mailingNamei;
		// prepare progressmonitor
		this.pm = new ProgressMonitor(this.parentWindow.getFrame(), "Aktionen anlegen", "", 0, this.allCB.getContacts()
				.size());
		this.pm.setMillisToPopup(5);
		this.pm.setMillisToDecideToPopup(5);
	}

	@Override
	protected Long doInBackground() throws Exception {
		int infoID = Aktion.easyNewAktionsInfo(mailingName);
		int i = 0;
		for (Contact c : allCB.getContacts()) {
			Aktion.easyNewAktion(c.getId(), Aktion.MAILINGRESULT, infoID, Aktion.MAILING);
			i++;
			// Set new state
			this.pm.setProgress(i);
			// Change the note if desired
			String state = "Angelegte Aktionen: " + i;
			this.pm.setNote(state);

			// if cancel is pressed in progressdialog
			if (this.pm.isCanceled()) {
				JOptionPane.showMessageDialog(this.parentWindow.getFrame(), "Es wurden " + i
						+ " Aktionen angelegt\nEs sind nicht für alle Kontakte Aktionen angelegt.");
				break;
			}
		}
		this.pm.close();
		return null;
	}
}

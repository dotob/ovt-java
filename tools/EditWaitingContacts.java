package tools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.xnap.commons.gui.table.TableSorter;

import ui.IMainWindow;
import db.DBTools;
import db.Database;
import db.Marktforscher;

/**
 * @author basti this show a dialog with a table in it that show all waiting
 *         contacts of one given marktforscher. now you can selectively set some
 *         of the waiting contact free.
 */
public class EditWaitingContacts implements ActionListener {

	private JDialog dialog;
	private Marktforscher mafo;
	private EditContactBundleTable bt;
	private TableSorter sorter;
	private IMainWindow parentWindow;

	/**
	 * construct a table for the given mafo
	 * 
	 * @param mafo
	 *            marktforscher whoms waiting contact are shown
	 */
	public EditWaitingContacts(Marktforscher mafo, IMainWindow pw) {
		this.mafo = mafo;
		this.parentWindow = pw;
		this.dialog = new JDialog(this.parentWindow.getFrame(), "Adressen freistellen");
		this.dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		this.dialog.getContentPane().add(this.createGUI());
		this.dialog.pack();
	}

	/**
	 * make a panel with a table and some aktionbuttons.
	 * 
	 * @return panel with gui-elements
	 */
	private Component createGUI() {
		JPanel ret = new JPanel(new BorderLayout());

		JPanel tablePanel = new JPanel();
		this.bt = new EditContactBundleTable();
		this.sorter = new TableSorter(this.bt.getBundleTableModel());
		JScrollPane inputScroller = new JScrollPane(this.bt);
		inputScroller.setPreferredSize(new Dimension(750, 650));
		tablePanel.add(inputScroller);

		JPanel buttons = new JPanel(new GridLayout(2, 0));
		JButton remove = new JButton("Ausgewählte Adressen freistellen");
		remove.setToolTipText("Die ausgwählten Adressen werden wieder auf frei gesetzt");
		remove.addActionListener(this);
		remove.setActionCommand("remove");
		buttons.add(remove);
		JButton close = new JButton("Beenden");
		close.setToolTipText("Schließen des Fensters");
		close.addActionListener(this);
		close.setActionCommand("close");
		buttons.add(close);

		ret.add(tablePanel, BorderLayout.NORTH);
		ret.add(buttons, BorderLayout.SOUTH);
		return ret;
	}

	/**
	 * load waiting contact and show dialog with gui.
	 */
	public void showMe() {
		this.bt.setCb(this.mafo.getWaitingContacts());
		this.dialog.setVisible(true);
		this.dialog.setEnabled(false);
		this.parentWindow.setWaitCursor();
		Thread t = new Thread(this.bt);
		t.start();
		this.parentWindow.setDefaultCursor();
		this.dialog.setEnabled(true);
	}

	/**
	 * set the selected contacts in table free, so other mafos can have them...
	 */
	private void removeContacts() {
		int[] sel = this.bt.getSelectedRows();
		if (sel.length > 0) {
			String toDel = "";
			for (int i = 0; i < sel.length; i++) {
				int j = sel[i];
				// check for table order...
				int row = this.sorter.mapToIndex(j);
				// get contact id
				String cID = (String) this.bt.getBundleTableModel().getValueAt(row, 0);
				toDel += cID + ",";
			}
			if (toDel.length() > 0) {
				toDel = toDel.substring(0, toDel.length() - 1);
			}
			String msg = "Möchten Sie die gewählten " + sel.length + " Adressen freistellen?";
			int answ = JOptionPane.showConfirmDialog(this.dialog, msg, "Adressen freistellen",
					JOptionPane.OK_CANCEL_OPTION);
			if (answ == JOptionPane.OK_OPTION) {
				System.out.println(toDel);
				try {
					// decrease counter from bereitgestellt
					MyLog.logDebug("reset contacts:" + toDel);
					ResultSet rs = Database.select("bereitgestellt, count(*), marktforscher", "kunden", "WHERE id IN ("
							+ toDel + ") GROUP BY bereitgestellt HAVING count(*)>0");
					while (rs.next()) {
						String bDate = "'" + rs.getDate(1).toString() + "'";
						String mf = rs.getString("marktforscher");
						int count = rs.getInt(2);
						// Database.update("bereitgestellt",
						// "count=count-"+count,
						// "WHERE wann="+bDate+" AND marktforscher="+mf+
						// " ORDER BY id DESC LIMIT 1");
						Date nowDate = new Date(new java.util.Date().getTime());
						Database.quickInsert("bereitgestellt", "NULL, " + bDate + ", " + mf + ", " + (-1 * count)
								+ ", 'reset: " + nowDate + "','<" + toDel + ">'");
						MyLog.logDebug("change bereitgestellt: " + mf + "|" + bDate + "|" + count);
					}
					// set contacts back
					DBTools.setContactsFree(toDel);
					// remove rows from table
					// for (int i : sel) {
					// this.bt.getBundleTableModel().removeRow(this.sorter.
					// mapToIndex(i));
					// }
				} catch (SQLException e) {
					MyLog.showExceptionErrorDialog(e);
				}
			}

			// reload
			this.showMe();
		}
	}

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getActionCommand().equals("close")) {
			this.dialog.setVisible(false);
			this.dialog.dispose();
		} else if (arg0.getActionCommand().equals("remove")) {
			this.removeContacts();
		}
	}
}

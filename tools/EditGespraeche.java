package tools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.xnap.commons.gui.table.TableLayout;
import org.xnap.commons.gui.table.TableSorter;

import ui.IMainWindow;
import adminassistent.GespraechsErfassungsPanel;
import db.Database;
import db.Gespraech;
import db.Marktforscher;

/**
 * @author basti this show a dialog with a table in it that show all gespraeche
 *         of mafo
 */
public class EditGespraeche implements ActionListener {

	private JDialog dialog;
	private Marktforscher mafo;
	private EditGespraecheTable gt;
	private TableSorter sorter;
	private GespraechsErfassungsPanel showGPanel;
	private IMainWindow parentWindow;

	public EditGespraeche(Marktforscher mafo, GespraechsErfassungsPanel showMePanel, IMainWindow pw) {
		this.mafo = mafo;
		this.parentWindow = pw;
		this.showGPanel = showMePanel;
		this.dialog = new JDialog(this.parentWindow.getFrame(), "Gespräche, Termine, Aufträge von " + mafo.toString());
		this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
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
		this.gt = new EditGespraecheTable(this.mafo.getGespraeche());
		this.sorter = new TableSorter(this.gt.getGespraecheTableModel());
		TableLayout layout = new TableLayout(this.gt, this.sorter);
		JScrollPane inputScroller = new JScrollPane(this.gt);
		inputScroller.setPreferredSize(new Dimension(800, 500));
		tablePanel.add(inputScroller);

		JPanel buttons = new JPanel(new GridLayout(2, 0));
		JButton remove = new JButton("Ausgewähltes Gespräch löschen");
		remove.addActionListener(this);
		remove.setActionCommand("remove");
		buttons.add(remove);
		JButton showIt = new JButton("Ausgewähltes Gespräch bearbeiten");
		showIt.addActionListener(this);
		showIt.setActionCommand("show");
		buttons.add(remove);
		JButton close = new JButton("Beenden");
		close.setToolTipText("Schließen des Fensters ohne Speichern");
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
		this.dialog.setVisible(true);
		this.parentWindow.setWaitCursor();
		this.gt.run();
		this.parentWindow.setDefaultCursor();
	}

	public void reload() {
		this.parentWindow.setWaitCursor();
		// reload gesprÃ¤che
		this.gt.setGespraeche(this.mafo.getGespraeche());
		// redo table
		this.gt.run();
		this.parentWindow.setDefaultCursor();
	}

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getActionCommand().equals("remove")) {
			if (this.gt.getSelectedColumnCount() == 1) {
				Gespraech g = (Gespraech) this.gt.getGespraecheTableModel().getValueAt(this.gt.getSelectedRow(), 0);
				int answ = JOptionPane.showConfirmDialog(null, "Soll das Gespräch " + g.getId()
						+ " wirklich gelöscht werden?");
				if (answ == JOptionPane.OK_OPTION) {
					Database.delete("gespraeche", "WHERE id=" + g.getId());
					this.reload();
					g.printMe();
				}
			}
		} else if (arg0.getActionCommand().equals("show")) {
			if (this.gt.getSelectedColumnCount() == 1) {
				Gespraech g = (Gespraech) this.gt.getGespraecheTableModel().getValueAt(this.gt.getSelectedRow(), 0);
				this.showGPanel.setGespraech(g);
			}

		} else if (arg0.getActionCommand().equals("close")) {
			this.dialog.setVisible(false);
			this.dialog.dispose();
		}
	}
}

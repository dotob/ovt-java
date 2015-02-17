package adminassistent;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import tools.DateInterval;
import tools.DateTool;
import tools.MyLog;
import ui.IMainWindow;
import ui.MFStateTableCellRenderer;
import db.Marktforscher;

/**
 * @author basti this shows a panel with a statistic table about a single or all
 *         mafos. one can create the honorarabrechnung from here. and edit
 *         wating contacts of a mafo.
 */
class MafoStateTable extends JTable implements Runnable {

	/**
	 * 
	 */
	private static final long		serialVersionUID	= 1L;
	private JButton					enableAfterThread;
	private MafoStateTableModel		mafoStateTableModel;
	private ProgressMonitor			pm;
	private Vector<Marktforscher>	mafoList;
	private String[]				monthNames			= { "Januar", "Februar", "März", "April", "Mai", "Juni",
			"Juli", "August", "September", "Oktober", "November", "Dezember" };
	private IMainWindow				parentWindow;

	/**
	 * the main ctor that creates a table and the aktion lists and buttons
	 * 
	 * @param pw
	 *            TODO
	 * @param billButton
	 *            the button for honorarabrechnung. model needs it to activate
	 *            or deactivate
	 * @param showWaitingButton
	 *            the button for edit waiting contacts. model needs it to
	 *            activate or deactivate
	 * @param resetButton
	 *            the button for maforesetting. model needs it to activate or
	 *            deactivate
	 */
	public MafoStateTable(IMainWindow pw) {
		this.parentWindow = pw;
		this.mafoStateTableModel = new MafoStateTableModel();
		this.setModel(this.mafoStateTableModel);
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		MFStateTableCellRenderer cellRenderer = new MFStateTableCellRenderer();
		try {
			this.setDefaultRenderer(Class.forName("java.lang.Object"), cellRenderer);
		} catch (ClassNotFoundException e) {
			MyLog.showExceptionErrorDialog(e);
			e.printStackTrace();
		}
		TableColumnModel tc = this.getColumnModel();
		int c = 0;
		tc.getColumn(c++).setPreferredWidth(200); // Name
		tc.getColumn(c++).setPreferredWidth(60); // Zeitraum
		tc.getColumn(c++).setPreferredWidth(80); // Bereitgestellt
		tc.getColumn(c++).setPreferredWidth(55); // wartend
		tc.getColumn(c++).setPreferredWidth(45); // Zurück
		tc.getColumn(c++).setPreferredWidth(95); // kein geld
		tc.getColumn(c++).setPreferredWidth(55); // Gespräche
		tc.getColumn(c++).setPreferredWidth(80); // Adressen/Gespräch
		tc.getColumn(c++).setPreferredWidth(30); // Zeit
		tc.getColumn(c++).setPreferredWidth(30); // Honorar
		tc.getColumn(c++).setPreferredWidth(30); // Honorar/Stunde

	}

	public void run() {
		this.parentWindow.setWaitCursor();
		int akt = 0;
		this.mafoStateTableModel.emptyMe();
		// collect date stuff
		int year = DateTool.actualAbrechnungsJahrAsInt();
		DateInterval jahr = DateTool.actualAbrechnungsJahr();
		int month = DateTool.actualAbrechnungsMonatAsInt();
		DateInterval monat = DateTool.actualAbrechnungsMonat();
		int week = DateTool.actualAbrechnungsWocheAsInt();
		DateInterval woche = DateTool.actualAbrechnungsWoche();

		if (year > 0 && month > 0 && week > 0) {
			for (Iterator<Marktforscher> iter = mafoList.iterator(); iter.hasNext();) {
				Marktforscher mafo = iter.next();
				mafo.setNameStyle(Marktforscher.NAMESTYLE_WITHGROUP);

				Marktforscher mafoShowNoName = Marktforscher.SearchMarktforscher(mafo.getId());
				mafoShowNoName.setNameStyle(Marktforscher.NAMESTYLE_NONE);

				// Set new state
				pm.setProgress(akt);
				akt += 10;
				// Change the note if desired
				String state = "Bearbeite: " + mafo;
				pm.setNote(state);

				// collect data
				int waiting = mafo.getWaitingContactsCount();
				this.parentWindow.setStatusText(state + ": Jahr");
				if (year > 0) {
					String waitingYear = "";
					int providedYear = mafo.getProvidedContacts(jahr);
					int finishedYear = mafo.getFinishedContactsCount(jahr);
					String providedYearStr = Integer.toString(providedYear);
					if ((finishedYear + waiting) != providedYear) {
						providedYearStr += " (" + (providedYear - (finishedYear + waiting)) + ")";
					}
					int noMoneyYear = mafo.getNoMoneyContactsCount(jahr);
					int terminCountYear = mafo.getHonorierbareTermine(jahr).size();
					this.mafoStateTableModel.addMafoStateRow(mafo, Integer.toString(year), providedYearStr,
							waitingYear, finishedYear, noMoneyYear, terminCountYear);
				} else {
					System.out.println("Aktuelles Jahr nicht gefunden");
				}

				// view only actual month
				this.parentWindow.setStatusText(state + ": Monat");
				if (month > 0) {
					String waitingMonth = "";
					int providedMonth = mafo.getProvidedContacts(monat);
					int finishedYear = mafo.getFinishedContactsCount(monat);
					int noMoneyMonth = mafo.getNoMoneyContactsCount(monat);
					int terminCountMonth = mafo.getHonorierbareTermine(monat).size();
					String providedMonthStr = Integer.toString(providedMonth);
					// TODO: makes no sense cause when nonth changes there are
					// miscounts...
					// if ((finishedYear+waiting)!=providedMonth){
					// providedMonthStr += "
					// ("+(providedMonth-(finishedYear+waiting))+")";
					// }
					this.mafoStateTableModel.addMafoStateRow(mafoShowNoName, this.monthNames[month - 1],
							providedMonthStr, waitingMonth, finishedYear, noMoneyMonth, terminCountMonth);
				} else {
					System.out.println("Aktueller Monat nicht gefunden");
				}

				// view live data, means this weeks data
				this.parentWindow.setStatusText(state + ": Live");
				if (week > 0) {
					String waitingLive = Integer.toString(waiting);
					int providedLive = mafo.getProvidedContacts(woche);
					int finishedLive = mafo.getFinishedContactsCount(woche);
					int noMoneyLive = mafo.getNoMoneyContactsCount(woche);
					int terminCountLive = mafo.getHonorierbareTermine(woche).size();
					this.mafoStateTableModel.addMafoStateRow(mafoShowNoName, week + ". Woche", Integer
							.toString(providedLive), waitingLive, finishedLive, noMoneyLive, terminCountLive);
				} else {
					System.out.println("Aktuelle Woche nicht gefunden");
				}

				// if cancel is pressed in progressdialog
				if (pm.isCanceled()) {
					JOptionPane.showMessageDialog(this.parentWindow.getFrame(),
							"Anzeige der Marktforscherdaten kann unvollständig sein!");
					break;
				}
			}
			// check if something was added
			if (this.mafoStateTableModel.getRowCount() > 0) {
				this.clearSelection();
				this.addRowSelectionInterval(0, 0);
			}
		} else {
			JOptionPane.showMessageDialog(this.parentWindow.getFrame(), "Konnte die Zeitintervalle nicht bestimmen.");
		}
		pm.close();
		if (this.enableAfterThread != null) {
			this.enableAfterThread.setEnabled(true);
		}
		this.parentWindow.setDefaultCursor();
	}

	public Vector<Marktforscher> getMafoList() {
		return mafoList;
	}

	public void setMafoList(Vector<Marktforscher> mafoList) {
		this.mafoList = mafoList;
	}

	public Marktforscher getSelectedMarktforscher() {
		return (Marktforscher) this.mafoStateTableModel.getValueAt(this.getSelectedRow(), 0);
	}

	public Vector<Marktforscher> getAllMarktforscherToBill() {
		Vector<Marktforscher> ret = new Vector<Marktforscher>();
		int anz = (int) (this.mafoStateTableModel.getRowCount() / 3);
		for (int i = 0; i < anz; i++) {
			ret.add((Marktforscher) this.mafoStateTableModel.getValueAt(i * 3, 0));
		}
		return ret;
	}

	public ProgressMonitor getPm() {
		return pm;
	}

	public void setPm(ProgressMonitor pm) {
		this.pm = pm;
	}

	public MafoStateTableModel getMafoStateTableModel() {
		return mafoStateTableModel;
	}

	public void setEnableButton(JButton button) {
		this.enableAfterThread = button;
	}
}

// ================ tablemodel =====================================
/**
 * @author basti model for mafostatistictable. there are always 3 lines per
 *         mafo. with different data in it.
 */
class MafoStateTableModel extends DefaultTableModel {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	public MafoStateTableModel() {
		super();
		Vector<String> cn = new Vector<String>();
		cn.add(new String("Name"));
		cn.add(new String("Zeit"));
		cn.add(new String("Bereit"));
		cn.add(new String("Wartend"));
		cn.add(new String("Fertig"));
		cn.add(new String("Davon kein \u20ac"));
		cn.add(new String("honTerm"));
		cn.add(new String("Adr./honTerm"));
		cn.add(new String("Zeit"));
		cn.add(new String("\u20ac"));
		cn.add(new String("\u20ac/h"));
		this.setColumnIdentifiers(cn);
	}

	public boolean isCellEditable(int row, int col) {
		boolean ret = false;
		return ret;
	}

	@SuppressWarnings("unchecked")
	public void addMafoStateRow(Marktforscher mafo, String range, String provided, String waiting, int ready,
			int noMoney, int honorierbareTermineCount) {
		Vector tableRow = new Vector();
		tableRow.add(mafo);
		tableRow.add(range);
		tableRow.add(provided);
		tableRow.add(waiting);
		tableRow.add(ready);
		tableRow.add(noMoney);
		// check for week
		if (range.indexOf("Woche") >= 0) {
			tableRow.add("");// honterm
			tableRow.add("");// Adressen/Termin
			// zeit
			if (ready > 0) {
				tableRow.add(ready / 15);
			} else {
				tableRow.add("0");
			}
			tableRow.add("");// honorar
			tableRow.add("");// honorar/stunde

		} else {
			// honterm
			tableRow.add(Integer.toString(honorierbareTermineCount));
			// Adressen/Termin
			if (honorierbareTermineCount > 0) {
				tableRow.add(Math.round((ready - noMoney) / honorierbareTermineCount));
			} else {
				tableRow.add("n/a");
			}
			// zeit
			if (ready > 0) {
				tableRow.add(ready / 15);
			} else {
				tableRow.add("0");
			}
			// honorar
			int honorarSummeTermine = (int) (honorierbareTermineCount * mafo.getHonorarTermin());
			int honorarSummeTelefon = (int) (Math.round((ready - noMoney)
					* (mafo.getHonorarPauschale() + mafo.getHonorarAdresse())));
			int komplettSummeHonorar = honorarSummeTermine + honorarSummeTelefon;
			tableRow.add(komplettSummeHonorar);
			// honorar/stunde
			if (ready > 0) {
				tableRow.add((int) (Math.round(komplettSummeHonorar / ((ready - noMoney) / 15d))));
			} else {
				tableRow.add("n/a");
			}
		}
		this.addRow(tableRow);
	}

	public void emptyMe() {
		setRowCount(0);
	}
}

package statistics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker.StateValue;

import org.xnap.commons.gui.DirectoryChooser;

import statistics.MoreMFCollector.whatToDo;
import tools.SettingsReader;
import ui.IMainWindow;

public class MoreMF extends JPanel implements ActionListener, PropertyChangeListener {

	private static final long serialVersionUID = 1L;

	private MoreMFCollector dataCollector;

	private static final String name = "Mehr MF & WL";

	private JTextField dir;
	private JButton goMotiButton;
	private JButton goMFErfolgButton;
	private JButton goMFSolarErfolgButton;
	private JButton goWLErfolgButton;

	public MoreMF(IMainWindow pw) {
		dataCollector = new MoreMFCollector(pw);
		initGUI();
	}

	private void initGUI() {
		this.setLayout(new BorderLayout());

		JPanel mf = new JPanel(new GridLayout(0, 1, 0, 3));
		JButton dirSelBut = new JButton("Exportverzeichnis... ");
		dirSelBut.setActionCommand("importdir");
		dirSelBut.addActionListener(this);
		mf.add(dirSelBut);
		this.dir = new JTextField(SettingsReader.getString("OVTAdmin.mfMotivationsVerzeichnis"));
		mf.add(this.dir);
		goMotiButton = new JButton("MF Motivation: Excel-Dateien erzeugen");
		goMotiButton.setPreferredSize(new Dimension(100, 40));
		goMotiButton.setActionCommand("startMoti");
		goMotiButton.addActionListener(this);
		mf.add(goMotiButton);
		goMFErfolgButton = new JButton("<html>MF Erfolgsrechnung <b>F & HT</b>: Excel-Dateien erzeugen</html>");
		goMFErfolgButton.setPreferredSize(new Dimension(100, 40));
		goMFErfolgButton.setActionCommand("startMFErfolg");
		goMFErfolgButton.addActionListener(this);
		mf.add(goMFErfolgButton);
		goMFSolarErfolgButton = new JButton("<html>MF Erfolgsrechnung <b>Solar</b>: Excel-Dateien erzeugen</html>");
		goMFSolarErfolgButton.setPreferredSize(new Dimension(100, 40));
		goMFSolarErfolgButton.setActionCommand("startMFErfolgSolar");
		goMFSolarErfolgButton.addActionListener(this);
		goMFSolarErfolgButton.setEnabled(false);
		mf.add(goMFSolarErfolgButton);
		goWLErfolgButton = new JButton("WL Erfolgsrechnung: Excel-Dateien erzeugen");
		goWLErfolgButton.setPreferredSize(new Dimension(100, 40));
		goWLErfolgButton.setActionCommand("startWLErfolg");
		goWLErfolgButton.addActionListener(this);
		mf.add(goWLErfolgButton);
		this.add(mf, BorderLayout.NORTH);
	}

	public void doStatistics(String cmd) {
		if (this.dir.getText().length() > 0) {
			File f = new File(this.dir.getText());
			if (f.exists() && f.canWrite() && f.isDirectory()) {
				if (cmd.equals("startMoti")) {
					this.dataCollector.setWhichStatistic(whatToDo.MFMOTIVATION);
				} else if (cmd.equals("startMFErfolg")) {
					this.dataCollector.setWhichStatistic(whatToDo.MFERFOLG);
				} else if (cmd.equals("startMFErfolgSolar")) {
					this.dataCollector.setWhichStatistic(whatToDo.MFERFOLGSOLAR);
				} else if (cmd.equals("startWLErfolg")) {
					this.dataCollector.setWhichStatistic(whatToDo.WLERFOLG);
				}
				this.dataCollector.setOutputDir(this.dir.getText());
				this.dataCollector.addPropertyChangeListener(this);
				// set buttons off
				toggleActionButtons(false);
				// start collecting
				this.dataCollector.execute();
			} else {
				JOptionPane.showMessageDialog(this, "Probleme mit dem Verzeichnis.");
			}
		} else {
			JOptionPane.showMessageDialog(this, "Kein Verzeichnis ausgewählt.");
		}
	}

	private void toggleActionButtons(boolean enabled) {
		this.goMFErfolgButton.setEnabled(enabled);
		// this.goMFSolarErfolgButton.setEnabled(enabled);
		this.goMotiButton.setEnabled(enabled);
		this.goWLErfolgButton.setEnabled(enabled);
	}

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getActionCommand().equals("startMoti") || arg0.getActionCommand().startsWith("startMFErfolg")) {
			this.doStatistics(arg0.getActionCommand());

		} else if (arg0.getActionCommand().equals("importdir")) {
			DirectoryChooser dialog = new DirectoryChooser();
			dialog.setTitle("Export-Verzeichnis wählen");
			String eDir = "";
			if (dialog.showChooseDialog(this) == DirectoryChooser.APPROVE_OPTION) {
				eDir = dialog.getSelectedDirectory().getAbsolutePath();
				this.dir.setText(eDir);
			}
			SettingsReader.setValue("OVTAdmin.mfMotivationsVerzeichnis", eDir);
			SettingsReader.saveProperties();
		}
	}

	public String getTabName() {
		return name;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// only do stuff if swingworker is done
		if ("state".equals(evt.getPropertyName()) && evt.getNewValue().equals(StateValue.DONE)) {
			// what to do when collection is finished
			toggleActionButtons(true);
		}
	}
}

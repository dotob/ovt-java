package datenerfasser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;

import tools.MafoInfoTable;
import tools.MyLog;


import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;

import db.Database;

/**
 * @author basti
 * this is the main tool for the marktforscher who sits on a phone and calls some adresses. 
 * it can show statistic of calls and lets the mafo create new aktions for a contact.
 */
public class Datenerfasser  implements ActionListener{
	private final static String versionString   = "0.1";
	private final static String applicationName = "OVT Datenerfasser";
	
	private static JFrame frame;
	private static JLabel statusBarText;
	
	// attr
	private static JProgressBar  statusBarProgress;
	
	// gui
	private MafoInfoTable mfInfoTable;
	
	/**
	 * Create the GUI and show it. 
	 * this check if a mafo is set and asks for it if not. it checks for 
	 * updates of programm ad builds the gui
	 */
	private static void createAndShowGUI() {
		PlasticLookAndFeel.setTabStyle(PlasticLookAndFeel.TAB_STYLE_METAL_VALUE);
		try {
			UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
		} catch (Exception e) {}
		//Create and set up the window.
		frame = new JFrame(applicationName+" "+versionString);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// check for database
		if (Database.test()){
			Datenerfasser app  = new Datenerfasser();
			
			MyLog.logDebug("in the middle of nowhere");
			
			Component contents = app.createComponents();
			Container contentPane = frame.getContentPane();
			contentPane.setLayout(new BorderLayout());
			contentPane.add(contents, BorderLayout.CENTER);
			statusBarText = new JLabel("  ");
			contentPane.add(statusBarText, BorderLayout.SOUTH);
			
			
			// Display the window. in the center of the screen
			Toolkit   tk     = Toolkit.getDefaultToolkit ();
			Dimension screen = tk.getScreenSize ();
			int sw = (int)screen.getWidth();
			int sh = (int)screen.getHeight();
			int myW = Math.min( (int)(sw*0.9), 800);
			int myH = Math.min( (int)(sh*0.9), 680);
			Dimension mySize = new Dimension(myW, myH);
			frame.setPreferredSize(mySize);
			int x =  (int) (sw-mySize.getWidth())/2;
			int y =  (int) (sh-mySize.getHeight())/2;
			frame.setLocation(x, y);
			frame.setIconImage(new ImageIcon("resources/ovt-mafo-frame.gif").getImage());
			
			frame.pack();
			frame.setVisible(true);
			
		} else {
			MyLog.logError("could not load db: "+Database.dbURL());
			JLabel error = new JLabel("<html>Konnte nicht auf die Datenbank zugreifen!" +
					"<br>DB-Url: "+Database.dbURLNoPass()+"</html>");
			error.setFont(new Font("Tahoma", Font.BOLD, 24));
			frame.getContentPane().add(error, BorderLayout.CENTER);
			// Display the window.
			Dimension mySize = new Dimension(500, 500);
			frame.setPreferredSize(mySize);
			// Get the screen dimensions.
			Toolkit   tk     = Toolkit.getDefaultToolkit ();
			Dimension screen = tk.getScreenSize ();
			MyLog.logDebug("MF-Assi Start mit Bildschirmauflösung: "+screen);
			int x =  (int) (screen.getWidth()-mySize.getWidth())/2;
			int y =  (int) (screen.getWidth()-mySize.getHeight())/2;
			System.out.println(x+":"+y);
			frame.setLocation(x, y);
			
			frame.pack();
			frame.setVisible(true);
		}
	}
	
	/**
	 * fill main panel with components. main panel shows a tabbedpane with panes for information, 
	 * contact working and about the program
	 * @return the components for main panel
	 */
	public Component createComponents(){
		JPanel mainPanel     = new JPanel(new BorderLayout());
		mainPanel.add(new MySearchContactPanel());
		return mainPanel;
	}
	
	
	// ================ actions =====================================
	public void actionPerformed(ActionEvent e) {
		String com = e.getActionCommand(); 
		
	}
	
	public static JFrame getFrame() {
		return frame;
	}
	
	/**
	 * set text of my own little status bar
	 * @param txt text to set a status message
	 */
	public static void setStatusText(String txt){
		statusBarText.setText(txt);
	}
	public static void setWaitCursor(){
		frame.getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
	}
	
	public static void setDefaultCursor(){
		frame.getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	/**
	 * set text of my own little status bar
	 * @return txt text of the status message
	 */
	public static String getStatusText(){
		return statusBarText.getText();
	}
	
	public static void startStatusProgress(){
		statusBarProgress.setIndeterminate(true);
	}
	
	public static void stopStatusProgress(){
		statusBarProgress.setIndeterminate(false);
	}
	
	//	 ================ main =====================================
	//	 ================ main =====================================
	//	 ================ main =====================================
	//	 ================ main =====================================
	//	 ================ main =====================================
	//	 ================ main =====================================
	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
	
	public MafoInfoTable getMfInfoTable() {
		return mfInfoTable;
	}
}

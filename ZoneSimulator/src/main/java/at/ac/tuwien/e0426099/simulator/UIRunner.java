package at.ac.tuwien.e0426099.simulator;

import at.ac.tuwien.e0426099.simulator.ui.MainFrame;

import javax.swing.*;

/**
 * @author PatrickF
 * @since 06.12.12
 */
public class UIRunner {

	private static void setLookAndFeel() {
		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setLookAndFeel();
				new MainFrame();
			}
		});
	}
}

package at.ac.tuwien.e0426099.simulator.ui;

import javax.swing.*;

/**
 * @author PatrickF
 * @since 06.12.12
 */
public class MainFrame extends JFrame {


	public MainFrame() {
		setup();
	}

	private void setup() {

		setName("Hello World");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Add the ubiquitous "Hello World" label.
		JLabel label = new JLabel("Hello World");

		getContentPane().add(label);


		pack();
		setVisible(true);
	}


}

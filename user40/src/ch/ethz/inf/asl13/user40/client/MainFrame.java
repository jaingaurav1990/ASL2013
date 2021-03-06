package ch.ethz.inf.asl13.user40.client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import ch.ethz.inf.asl13.user40.*;

/**
 * The main window for graphical and interactive client.
 */
public class MainFrame extends JFrame {

	/** Set to <tt>true</tt> if we're running on Mac OS X. */
	public static final boolean onMacOSX
		= "Mac OS X".equals(System.getProperty("os.name"));

	public MainFrame() {
		super();

		final MessagingService service = MessagingServiceClient.getPort();

		final JTextPane text = new JTextPane();
		final JComboBox queueSelection = new JComboBox();
		final JComboBox prioritySelection = new JComboBox(new Integer[]{ 1, 2, 3, 4, 5 });
		final JButton sendButton = new JButton("Send");

		text.setText("Hello");

		ActionListener refreshQueues = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				queueSelection.removeAllItems();
				for(int id : service.listQueues()) {
					queueSelection.addItem(Integer.valueOf(id));
				}

				// add queue that doesn't exist
				queueSelection.addItem(Integer.valueOf(-23));
			}
		};
		refreshQueues.actionPerformed(null);

		sendButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Message message = new Message(text.getText());
				message.priority = ((Integer)prioritySelection.getSelectedItem()).intValue();
				message.queueId = ((Integer)queueSelection.getSelectedItem()).intValue();
				service.insertMessage(message);
			}
		});

		JPanel bottom = new JPanel();
		bottom.setAlignmentX(1f);
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
		bottom.add(queueSelection);
		bottom.add(Box.createRigidArea(new Dimension(5, 0)));
		bottom.add(prioritySelection);
		bottom.add(Box.createRigidArea(new Dimension(5, 0)));
		bottom.add(sendButton);

		add(text, BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);

		setTitle("Graphical Messaging Client");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(350, 300);
	}

	/**
	 * Entry point for the application.
	 */
	public static void main(String[] args) {

		// perform platform-specific initialization
		if (onMacOSX) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
		}

		// load the appropriate look and feel
		// for the platform we're running on
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {
			System.err.println(e);
		}

		new MainFrame().setVisible(true);
	}
}
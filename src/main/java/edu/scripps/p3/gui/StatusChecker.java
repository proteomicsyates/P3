/**
 * diego
 * Jun 10, 2013
 */
package edu.scripps.p3.gui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author diego
 * 
 */
public class StatusChecker {

	private JProgressBar progressBar;
	private JTextArea taskOutput;
	JFrame frame;

	public StatusChecker(String title, int max) {

		frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		progressBar = new JProgressBar(0, max);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		taskOutput = new JTextArea(10,25);
		taskOutput.setMargin(new Insets(5, 5, 5, 5));
		taskOutput.setEditable(false);

		panel.add(progressBar);
		panel.add(new JScrollPane(taskOutput));
	
		frame.add(panel);
		
		// Display the window.
		frame.pack();
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int frame_w = frame.getSize().width;
		int frame_h = frame.getSize().height;
		frame.setLocation((dim.width - frame_w) / 2, (dim.height - frame_h) / 2);
		
		frame.setVisible(true);
		frame.setAlwaysOnTop(true);

	}

	public void setStatus(int val, String text) {

		progressBar.setValue(val);
		taskOutput.append(text + "\n");

		if (val == progressBar.getMaximum()) {
			frame.dispose();
		}

	}
	
	public void dispose() {
		frame.dispose();
	}
	
}

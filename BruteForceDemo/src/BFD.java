import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class BFD {
	static String zipPath=null;
	static String passwordListPath=null;
	
	public static void main(String[] args) throws IOException {
		JFrame loadPanel = new JFrame("BFD");
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		loadPanel.setMinimumSize(new Dimension(400, 180));
		loadPanel.setMaximumSize(new Dimension(400, 180));
		loadPanel.setLocation(dim.width / 2 - loadPanel.getSize().width / 2, dim.height / 2 - loadPanel.getSize().height / 2);
		loadPanel.getContentPane().setLayout(new GridBagLayout());
		loadPanel.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		GridBagConstraints gbc = new GridBagConstraints();
		loadPanel.getContentPane().setLayout(new GridBagLayout());
		JLabel jti = new JLabel("<html><i>Drag and Drop ZIP File</i><br><br></html>");
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth=2;
		loadPanel.getContentPane().add(jti, gbc);
		JLabel jtinfo = new JLabel("Bruteforcing a-z & 0-9");
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth=1;
		gbc.gridx = 0;
		gbc.gridy = 1;
		loadPanel.getContentPane().add(jtinfo, gbc);

		JLabel jtmin = new JLabel("Min PW Length");
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 2;
		loadPanel.getContentPane().add(jtmin, gbc);
		JTextField jtfmin = new JTextField(2);
		jtfmin.setText("3");
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 2;
		loadPanel.getContentPane().add(jtfmin, gbc);
		JLabel jtmax = new JLabel("Max PW Length");
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 3;
		loadPanel.getContentPane().add(jtmax, gbc);	
		JTextField jtfmax = new JTextField(2);
		jtfmax.setText("4");
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 3;
		loadPanel.getContentPane().add(jtfmax, gbc);	
		
		BruteForceZIP bfz = new BruteForceZIP();
		
		JButton bc = new JButton("Start Brute-Force");
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 4;
		loadPanel.getContentPane().add(bc, gbc);
		bc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadPanel.dispose();
			    new Thread(new Runnable() {
			        public void run() {
			        	bfz.start(zipPath,passwordListPath,jtfmin.getText(),jtfmax.getText());
			        }
			    }).start();
			}
		});

		bc.setEnabled(false);

		new FileDrop(loadPanel, new FileDrop.Listener() {
			public void filesDropped(java.io.File[] files) {
				for (File file : files) {
					if (file.getName().toLowerCase().endsWith("zip")) {
						jti.setText("<html>"+file.getName()+"<br>Optional: Drag and Drop Passwordlist<br><br></html>");
						zipPath=file.getAbsolutePath();
						bc.setEnabled(true);
					}else if(zipPath!=null){
						passwordListPath = file.getAbsolutePath();
						jti.setText("<html>"+zipPath+"<br>using password list.<br><br></html>");
						bc.setText("Start Passwordlist");
						jtfmax.setEnabled(false);
						jtfmin.setEnabled(false);
						jtmin.setEnabled(false);
						jtmax.setEnabled(false);
						jtinfo.setText("");
					}
				}
			}
		});
		loadPanel.pack();
		loadPanel.setVisible(true);
	}


}
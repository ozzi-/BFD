import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

public class BFD {
	static String zipPath=null;
	static String passwordListPath=null;
	
	public static void main(String[] args) throws IOException {
		JFrame loadPanel = new JFrame("BFD");
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		loadPanel.setMinimumSize(new Dimension(400, 120));
		loadPanel.setMaximumSize(new Dimension(400, 120));
		loadPanel.setLocation(dim.width / 2 - loadPanel.getSize().width / 2, dim.height / 2 - loadPanel.getSize().height / 2);
		loadPanel.getContentPane().setLayout(new GridBagLayout());
		loadPanel.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		JLabel jti = new JLabel("Drag and Drop ZIP File");
		loadPanel.add(jti);
		JButton bc = new JButton("Start Brute-Force");
		
		BruteForceZIP bfz = new BruteForceZIP();
		
		bc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadPanel.dispose();
			    new Thread(new Runnable() {
			        public void run() {
			        	bfz.start(zipPath,passwordListPath);
			        }
			    }).start();
			}
		});

		loadPanel.add(bc);
		bc.setEnabled(false);

		new FileDrop(loadPanel, new FileDrop.Listener() {
			public void filesDropped(java.io.File[] files) {
				for (File file : files) {
					if (file.getName().toLowerCase().endsWith("zip")) {
						jti.setText("<html>"+file.getName()+"<br>Optional: Drag and Drop Passwordlist</html>");
						zipPath=file.getAbsolutePath();
						bc.setEnabled(true);
					}else if(zipPath!=null){
						passwordListPath = file.getAbsolutePath();
						jti.setText("<html>"+zipPath+"<br>using password list "+passwordListPath+"</html>");
					}
				}
			}
		});
		loadPanel.pack();
		loadPanel.setVisible(true);
	}


}
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class BruteForceZIP {
	
	private JFrame jf;
	private JProgressBar jpb;
	private JTextField jtf;
	private JLabel jl;

	public BruteForceZIP() {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		jf = new JFrame("BFD");
		jf.setResizable(false);
		GridBagConstraints gbc = new GridBagConstraints();
		jf.getContentPane().setLayout(new GridBagLayout());
		jtf = new JTextField("Status", 12);
		jtf.setEnabled(false);
		jtf.setDisabledTextColor(new Color(20, 20, 200));
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		jf.getContentPane().add(jtf, gbc);
		jpb = new JProgressBar();
		jpb.setStringPainted(true);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 0;
		jf.getContentPane().add(jpb, gbc);
		jl = new JLabel("Running");
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 2;
		gbc.gridx = 0;
		gbc.gridy = 1;
		jf.getContentPane().add(jl, gbc);
		jf.pack();
		jf.setLocation(dim.width / 2 - jf.getSize().width / 2, dim.height / 2 - jf.getSize().height / 2);
		jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);	}
	
	public void bruteForceZIP(String zipPath, String passwordListPath) throws IOException {
	
		jf.setVisible(true);

		File zP = new File(zipPath);
		if (!zP.exists() || zP.isDirectory()) {
			System.err.println(zipPath + " can not be found");
			System.exit(1);
		}
		List<String> pwlist = null;
		String passListPath = null;

		String path = BFD.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String runPath = URLDecoder.decode(path, "UTF-8");
		long start = System.currentTimeMillis();

		jf.revalidate();
		jf.repaint();

		
		int i = 0;

		if (passwordListPath != null) {
			jl.setText("Running using passwordlist");
			passListPath = passwordListPath;
			File fP = new File(passListPath);
			if (!fP.exists() || fP.isDirectory()) {
				System.err.println(passListPath + " can not be found");
				System.exit(1);
			}
			pwlist = Files.readAllLines(new File(passListPath).toPath(), Charset.defaultCharset());
			boolean skipOutputForPerformance = pwlist.size() > 100;
			for (String pw : pwlist) {
				i++;
				boolean res = Util.decryptAndUnzip(zipPath, pw, runPath);
				if (res) {
					long end = System.currentTimeMillis();
					jtf.setText(pw);
					jl.setText("Password found in " + (end - start) / 1000 + "s : " + pw);
				}
				if ((skipOutputForPerformance && i % 50 == 0) || !skipOutputForPerformance || i == pwlist.size()) {
					jpb.setValue((i * 100 / pwlist.size()));
					jtf.setText(pw);
				}
			}
			jl.setText("Could not find valid password");
		} else {
			jl.setText("Running using Brute-Force Permutation");
			List<Character> characters = new ArrayList<Character>();

			for (char c = 'a'; c <= 'z'; c++) {
				characters.add(c);
			}
			for (char c = '0'; c <= '9'; c++) {
				characters.add(c);
			}
			boolean foundPW = false;
			int tot = characters.size() * characters.size() * characters.size() * characters.size();
			outerloop:
			for (Character c : characters) {
				for (Character d : characters) {
					for (Character e : characters) {
						for (Character f : characters) {
							String pw = "" + c + d + e + f;
							i++;
							boolean res = Util.decryptAndUnzip(zipPath, pw, runPath);
							
							if (res) {
								long end = System.currentTimeMillis();
								jtf.setText(pw);
								jl.setText("Password found in " + (end - start) / 1000 + "s : " + pw);
								foundPW = true;
								break outerloop;
							}
							if (i % 100 == 0) {
								jtf.setText(pw);
								jpb.setValue(i * 100 / tot);
							}
						}

					}
				}
			}
			if (!foundPW) {
				jl.setText("Could not find password");
			}
		}
	}

	public void start(String zipPath, String passwordListPath) {
		jf.setVisible(true);
		try {
			bruteForceZIP(zipPath,passwordListPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

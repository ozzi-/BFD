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
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class BruteForceZIP {
	
	private JFrame jf;
	private JProgressBar jpb;
	private JTextField jtf;
	private static JLabel jl;
	private int min;
	private int max;
	private static String lastPW;
	
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
			JOptionPane.showMessageDialog(jf,
				    "Zip could not be found",
				    "Error",
				    JOptionPane.ERROR_MESSAGE);
			return;
		}
		List<String> pwlist = null;
		String passListPath = null;

		String path = BFD.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String runPath = URLDecoder.decode(path, "UTF-8");
		if(runPath.toLowerCase().endsWith(".jar")) {
			runPath = runPath.substring(0, runPath.lastIndexOf('.'));
		}
		long start = System.currentTimeMillis();

		jf.revalidate();
		jf.repaint();

		int i = 0;
		if (passwordListPath != null) {
			jl.setText("Running using passwordlist");
			passListPath = passwordListPath;
			File fP = new File(passListPath);
			if (!fP.exists() || fP.isDirectory()) {
				JOptionPane.showMessageDialog(jf,
					    "Passwordlist could not be found",
					    "Error",
					    JOptionPane.ERROR_MESSAGE);
				return;
			}
			pwlist = Files.readAllLines(new File(passListPath).toPath(), Charset.defaultCharset());
			boolean skipOutputForPerformance = pwlist.size() > 100;
			boolean foundpassword=false;
			for (String pw : pwlist) {
				i++;
				boolean res = Util.decryptAndUnzip(zipPath, pw, runPath);
				if (res) {
					long end = System.currentTimeMillis();
					jtf.setText(pw);
					jl.setText("Password found in " + (end - start) / 1000 + "s : " + pw);
					foundpassword=true;
					break;
				}
				if ((skipOutputForPerformance && i % 50 == 0) || !skipOutputForPerformance || i == pwlist.size()) {
					jpb.setValue((i * 100 / pwlist.size()));
					jtf.setText(pw);
				}
			}
			if(!foundpassword) {
				jl.setText("Could not find valid password");
			}
		} else {
			jl.setText("Running using Brute-Force Permutation");
			ArrayList<String> charSet2 = new ArrayList<String>();
			for (char c = 'a'; c <= 'z'; c++) {
				charSet2.add(String.valueOf(c));
			}
			for (char c = '0'; c <= '9'; c++) {
				charSet2.add(String.valueOf(c));
			}
			boolean foundPW = runCombinations(charSet2, zipPath, runPath, jtf, jl, jpb);
			if (!foundPW) {
				jtf.setText(lastPW);
				jl.setText("Could not find password");
			}
		}
	}
	
	// this could be done mathematically, but im too lazy and its fast enough
	private static long getCombinationCount(int charset, int min, int max) {
		int c = 0;
		for (int il = min; il <= max; il++) {
			int carry;
			int[] indices = new int[il];
			do {
				c++;
				carry = 1;
				for (int i = indices.length - 1; i >= 0; i--) {
					if (carry == 0)
						break;
					indices[i] += carry;
					carry = 0;
					
					if (indices[i] == charset) {
						carry = 1;
						indices[i] = 0;
					}
				}
			} while (carry != 1);
		}
		return c;
	}
	private boolean runCombinations(ArrayList<String> possibleValues, String zipPath, String runPath, JTextField jtf, JLabel jl2, JProgressBar jpb) {
		long start = System.currentTimeMillis();
		long tot = getCombinationCount(possibleValues.size(), min, max); 
		long c = 0 ;
		for (int il = min; il <= max; il++) {
			int carry;
			int[] indices = new int[il];
			do {
				String pw = "";
				for (int index : indices) {
					pw+=possibleValues.get(index);			
				}

				lastPW = pw;
				c++;
				boolean res=false;
				try {
					res = Util.decryptAndUnzip(zipPath, pw, runPath);
				}catch(Exception e) {
					e.printStackTrace();
				}
				
				if (res) {
					long end = System.currentTimeMillis();
					jtf.setText(pw);
					jl.setText("Password found in " + (end - start) / 1000 + "s : " + pw);
					return true;
				}
				if (c % 100 == 0) {
					jtf.setText(pw);
					jpb.setValue((int)(c * 100 / tot));
				}
				
				carry = 1;
				for (int i = indices.length - 1; i >= 0; i--) {
					if (carry == 0)
						break;
					
					indices[i] += carry;
					carry = 0;
					
					if (indices[i] == possibleValues.size()) {
						carry = 1;
						indices[i] = 0;
					}
				}
			} while (carry != 1);
		}
		return false;
	}

	public void start(String zipPath, String passwordListPath, String min, String max) {
		jf.setVisible(true);
		try {
			this.min = Integer.valueOf(min);
			this.max = Integer.valueOf(max);			
		}catch(Exception e) {
			this.min = 2;
			this.max = 3;
		}
		try {
			bruteForceZIP(zipPath,passwordListPath);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(jf,
				    e.getMessage(),
				    "Error",
				    JOptionPane.ERROR_MESSAGE);
		}
	}
}

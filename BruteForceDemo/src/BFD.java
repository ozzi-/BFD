import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;


// usage: java -jar BFD.jar /path/to/zip /path/to/passwordlist
public class BFD {
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("Usage: BFD.jar zip passwordlist");
			System.exit(1);
		}
		String zipPath = args[0];
		String passListPath = args[1];
		File zP = new File(passListPath);
		if (!zP.exists() || zP.isDirectory()) {
			System.err.println(passListPath + " can not be found");
			System.exit(1);
		}
		File fP = new File(passListPath);
		if (!fP.exists() || fP.isDirectory()) {
			System.err.println(passListPath + " can not be found");
			System.exit(1);
		}
		String path = BFD.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String runPath = URLDecoder.decode(path, "UTF-8");
		long start = System.currentTimeMillis();
		
		List<String> pwlist = Files.readAllLines(new File(passListPath).toPath(), Charset.defaultCharset());
		int i = 0;
		for (String pw : pwlist) {
			boolean res = decryptAndUnzip(zipPath, pw, runPath);
			if (res) {
				long end = System.currentTimeMillis();
				System.out.println("(" + (++i) + "/" + pwlist.size() + ") " + pw);
				System.out.println("****************************************");
				System.out.println("Password found in "+(end-start)+"ms : " + pw);
				System.out.println("****************************************");
				System.exit(0);
			}
			System.out.println("(" + (++i) + "/" + pwlist.size() + ") " + pw);
		}
		System.out.println("Could not decrypt zip with passwords provided");
		System.exit(2);
	}

	public static boolean decryptAndUnzip(String zipPath, String pw, String unzipPath) throws IOException {
		try {
		    ZipFile zipFile = new ZipFile(zipPath);
		    if (zipFile.isEncrypted()) {
		        zipFile.setPassword(pw);
		    }
		    zipFile.extractAll(unzipPath);
		} catch (ZipException e) {
			return false;
		}
		return true;
	}
}
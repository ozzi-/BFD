import java.io.IOException;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class Util {
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

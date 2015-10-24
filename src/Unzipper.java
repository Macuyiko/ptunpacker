import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Unzipper {
	public static void main(String... args) throws IOException {
		ZipInputStream zis = new ZipInputStream(new FileInputStream(args[0]));
		ZipEntry ze = zis.getNextEntry();
		while (ze != null) {
			String fileName = ze.getName();
			
			Pattern p = Pattern.compile("/([ES][0-9]+)/");
			Matcher m = p.matcher(fileName);
			String username = null;
			if (m.find())
				username = m.group(1);
			if (username == null || !fileName.endsWith("db.PT")) {
				ze = zis.getNextEntry();
				continue;
			}
			
			System.out.println(fileName);
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int len; byte[] buffer = new byte[1024];
            while ((len = zis.read(buffer)) > 0) {
            	bos.write(buffer, 0, len);
			}
            GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bos.toByteArray()));
            ByteArrayOutputStream bosu = new ByteArrayOutputStream();
            while((len = gis.read(buffer)) != -1){
                bosu.write(buffer, 0, len);
            }
            gis.close();
            PtReader reader = new PtReader(bosu.toByteArray());
            bosu.close();
            
            PrintWriter writer = new PrintWriter(username + ".csv", "UTF-8");
            writer.println(PtReader.getCsvHeader());
            for (PtReader.PTLine line : reader.getLines())
    			writer.println(line.toCsvLine());
            writer.close();
			ze = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();
	}
}

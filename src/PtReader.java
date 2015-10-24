import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PtReader {

	List<PTLine> lines;
	int pointer;
	byte[] byteArray;
	
	public static void main(String... args) throws IOException {
		PtReader reader = new PtReader(Paths.get("C:/Users/n11093/Desktop/db"));
		for (PTLine line : reader.getLines())
			System.out.println(line.toCsvLine());
	}
	
	public PtReader(Path input) throws IOException {
		this(Files.readAllBytes(input));
	}
	
	public PtReader(byte[] bytesg) throws IOException {
		lines = new ArrayList<PTLine>();
		byteArray = bytesg;
		pointer = 0;
		
		int version = rint();		
		if (version < 10)
			throw new IOException("Can only deal with version 10 or higher");
		
		int magic = rint();
		if (magic != 1347700294)
			throw new IOException("Magic number incorrect -- maybe forgot to unGZip file first");
		
		int ntags = rint();
        int bytes = ntags * (32 + 4); // tags
        bytes += 4; // minfilter
        bytes += 4; // foldlevel
        bytes += 6 * 4; // prefs
        
        for (int i=0;i<bytes;i++) rchr();
        
        readNode(0);
	}
	
	public List<PTLine> getLines() {
		return lines;
	}

	private void readNode(int level) {
		PTLine line = new PTLine();
		line.name = rstr();
		line.level = level;
		line.tag = rint();
		line.hidden = rchr();
        int numdays = rint();
        for (int i=0;i<numdays;i++) {
        	short date = rshr();
        	short firstminuteused = rshr();
        	line.activeseconds = rint();
        	line.semiidleseconds = rint();
        	line.key = rint();
        	line.lmb = rint();
        	line.rmb = rint();
        	line.scrollwheel = rint();
        	line.firsthour = firstminuteused / 60;
        	line.firstminute = firstminuteused - 60*(firstminuteused / 60);
        	line.day = (date & 0x1F);
        	line.month = (date >> 5 & 0xF);
        	line.year = 2000 + (date >> 9);
        }
        
        lines.add(line);
        
        int numchildren = rint();
        for (int i=0;i<numchildren;i++) {
            readNode(level + 1);
        }
	}
	
	private short rshr() {
		byte[] bytes = Arrays.copyOfRange(byteArray, pointer, pointer + 2);
		pointer += 2;
		short x = java.nio.ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).getShort();
		return x;
	}
	
	private int rint() {
		byte[] bytes = Arrays.copyOfRange(byteArray, pointer, pointer + 4);
		pointer += 4;
		int x = java.nio.ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
		return x;
	}

	private String rstr() {
		String z = "";
		char x;
		while ((x = rchr()) != '\0')
			z += x;
		return z;
	}
	
	private char rchr() {
		char x = (char) byteArray[pointer++];
		return x;
	}
	
	public static String getCsvHeader() {
		String r = "\"level\",\"name\",\"date\",\"firsttime\","
				+ "\"activeseconds\",\"semiidleseconds\","
				+ "\"keys\",\"lmb\",\"rmb\",\"scrollwheel\"";
		return r;
	}
	
	class PTLine {
		public int level;
		public String name;
		public int year, month, day;
		public int firsthour, firstminute;
		public int activeseconds, semiidleseconds;
		public int key, lmb, rmb, scrollwheel;
		public char hidden;
		public int tag;
		
		public String toString() {
			String r = "";
			for (int z=0;z<level;z++) r += " ";
        	r += name+" -- "+year+"-"+month+"-"+day+" "+
	    			firsthour+":"+firstminute+" "+
	    			activeseconds+"/"+semiidleseconds+
	    			" KEYS: "+key+" "+lmb+" "+rmb+" "+scrollwheel;
        	return r;
		}
		
		public String toCsvLine() {
			String r = level+","+"\""+name.replace("\"", "\"\"")+"\""+","+
					"\""+year+"-"+month+"-"+day+"\""+","+
					"\""+firsthour+":"+firstminute+"\""+","+
					activeseconds+","+semiidleseconds+","+
					key+","+lmb+","+rmb+","+scrollwheel;
			return r;
		}
		
		
		
	}
}

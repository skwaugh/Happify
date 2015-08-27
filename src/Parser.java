import java.io.*;
import java.util.*;


public class Parser {

	Line[] lines = new Line[51977];
	
	public Parser(String filename) {
		int linecount = 0;

		try { 
			File file = new File(filename);
			Scanner in = new Scanner(file);
			File out = new File("/Users/skwaugh/Desktop/corrected.txt");
			
			if (!out.exists()) {
				out.createNewFile();
			}
			
			FileWriter fw = new FileWriter(out.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			//headers
			bw.write("Beginning file...\n");
			bw.write(in.next()+ "\t"); //user_id
			bw.write(in.next()+ "\t"); //message_id
			bw.write(in.next()+ "\t"); //created_at
			in.next(); //tip		
			bw.write(in.next()+ "\t"); //short_text_1
			bw.write(in.next()+ "\t"); //short_text_2
			bw.write(in.next()+ "\t"); //short_text_3
			in.next(); //short_text_4	
			in.next(); //short_text_5
			in.next(); //message-
			in.next(); //happy_face
			in.next(); //track_id
			in.next(); //challenge_id
			bw.write(in.next()+ "\t"); //gender
			bw.write(in.next()+ "\t"); //age_range
			bw.write(in.next()+ "\t\n"); //employment_status
			
			while (in.hasNextLine()){//(linecount<51977) {
				String lineString = in.nextLine();
				
				if (lineString.equals("") || lineString.split("\t").length<13) {
					//System.out.println("String was empty");
				}
				else {
					Line line = new Line(lineString);
					bw.write(line.toString());
					lines[linecount++] = line;
				}
			}
			
			bw.close();
			fw.close(); //						
		}
		catch(FileNotFoundException fnf) {
			
		} catch (IOException e) {

		} catch (NullPointerException n) {
			System.out.println("nullpointer at line " + linecount);
		}
	}
	
	/*public static void main(String[] args) {
		Parser p = new Parser("grateful.txt");
		System.out.println("done");
	}*/
}

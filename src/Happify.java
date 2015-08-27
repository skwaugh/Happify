import java.io.*;
import java.util.*;

/*import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;*/
import opennlp.tools.chunker.*;
import opennlp.tools.cmdline.*;
import opennlp.tools.coref.*;
import opennlp.tools.dictionary.*;
import opennlp.tools.doccat.*;
import opennlp.tools.formats.*;
import opennlp.tools.namefind.*;
import opennlp.tools.ngram.*;
import opennlp.tools.parser.*;
import opennlp.tools.postag.*;
import opennlp.tools.sentdetect.*;
import opennlp.tools.stemmer.*;
import opennlp.tools.tokenize.*;
import opennlp.tools.util.*;
import opennlp.maxent.*;
import opennlp.model.*;
import opennlp.perceptron.*;
import opennlp.uima.postag.*;




public class Happify {

	public static void main(String[] args) {
		try {
			File file = new File("/Users/skwaugh/Desktop/corrected.txt");
			Scanner in = new Scanner(file);
			int linecount = 0;
			Line[] lines = new Line[50000];
			//int empty = 0;
			int i = 0;
			while (linecount<50000) {
				String lineString = in.nextLine();
				//System.out.println(lineString);
				if (lineString.equals("")) {
					//System.out.println("String was empty - " + linecount);
					//empty++;
				}
				else {
					Line line = new Line(lineString);
					lines[i++] = line;
					//System.out.println(line);
				}
				linecount++;
			}
			FreqDist fd = new FreqDist(lines);
			System.out.println("Distinct Words: " + fd.fdist.size());

			HashMap<String,Integer> fdistRefined = FreqDist.refineByFrequency(100, fd.fdist);
			HashMap<String,Integer> fdist2Refined = FreqDist.refineByFrequency(100, fd.fdist2);
			HashMap<String,Integer> fdist3Refined = FreqDist.refineByFrequency(100, fd.fdist3);
			HashMap<String,Integer> fdist4Refined = FreqDist.refineByFrequency(25, fd.fdist4);
			HashMap<String,Integer> fdist5Refined = FreqDist.refineByFrequency(25, fd.fdist5);


			System.out.println("Distinct words occurring at least 100 times: " + fdistRefined.size());
			String[] rank1 = fd.rankByFrequency(fdistRefined);
			String[] rank2 = fd.rankByFrequency(fdist2Refined);
			String[] rank3 = fd.rankByFrequency(fdist3Refined);
			String[] rank4 = fd.rankByFrequency(fdist4Refined);
			String[] rank5 = fd.rankByFrequency(fdist5Refined);





			String[] sent;
			/*
		    LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
			//HashMap<Integer, String> posLines = new HashMap<Integer,String>();
		    for (Line l : lines) {
				sent = l.short_text_1.split(" ");
			    List<CoreLabel> rawWords = Sentence.toCoreLabelList(sent);
			    Tree parse = lp.apply(rawWords);
			    //parse.pennPrint();
			    String pos = parse.toString();
			    //posLines.put(Integer.decode(l.message_id), pos);
			    bwParsed.write(l.message_id + "\t" + pos + "\n");
			}*/





			//

		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException");
		} catch (IOException i) {
			System.out.println("IOException");
		}

	}



/*

	public static void parse(String[] sent) {
		LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		//String[] sent = { "This", "is", "an", "easy", "sentence", "." };
		List<CoreLabel> rawWords = Sentence.toCoreLabelList(sent);
		Tree parse = lp.apply(rawWords);
		parse.pennPrint();
		System.out.println();
	}
*/


	public static void makeNgramStuff() throws IOException {
		File out1 = new File("/Users/skwaugh/Desktop/1gramsA.txt");
		File out2 = new File("/Users/skwaugh/Desktop/2gramsA.txt");
		File out3 = new File("/Users/skwaugh/Desktop/3gramsA.txt");
		File out4 = new File("/Users/skwaugh/Desktop/4gramsA.txt");
		File out5 = new File("/Users/skwaugh/Desktop/5gramsA.txt");
		//File outParsed = new File("/Users/skwaugh/Desktop/outParsed.txt");


		if (!out1.exists()) {
			out1.createNewFile();
		}

		if (!out2.exists()) {
			out2.createNewFile();
		}

		if (!out3.exists()) {
			out3.createNewFile();
		}

		if (!out4.exists()) {
			out4.createNewFile();
		}

		if (!out5.exists()) {
			out5.createNewFile();
		}
		/*if (!outParsed.exists()) {
			outParsed.createNewFile();
		} */
		
		FileWriter fw1 = new FileWriter(out1.getAbsoluteFile());
		BufferedWriter bw1 = new BufferedWriter(fw1);

		FileWriter fw2 = new FileWriter(out2.getAbsoluteFile());
		BufferedWriter bw2 = new BufferedWriter(fw2);

		FileWriter fw3 = new FileWriter(out3.getAbsoluteFile());
		BufferedWriter bw3 = new BufferedWriter(fw3);

		FileWriter fw4 = new FileWriter(out4.getAbsoluteFile());
		BufferedWriter bw4 = new BufferedWriter(fw4);

		FileWriter fw5 = new FileWriter(out5.getAbsoluteFile());
		BufferedWriter bw5 = new BufferedWriter(fw5);

		/*FileWriter fwParsed = new FileWriter(outParsed.getAbsoluteFile());
		BufferedWriter bwParsed = new BufferedWriter(fwParsed);
		 */
		/*for (Map.Entry<String,Integer> entry : fdistRefined.entrySet()) {
			bw1.write(entry.getKey() + " - " + entry.getValue() + "\n");
		}

		for (Map.Entry<String,Integer> entry : fdist2Refined.entrySet()) {
			bw2.write(entry.getKey() + " - " + entry.getValue() + "\n");
		}

		for (Map.Entry<String,Integer> entry : fdist3Refined.entrySet()) {
			bw3.write(entry.getKey() + " - " + entry.getValue() + "\n");
		}

		for (String s : rank1) {
			bw1.write(s + " - " + fdistRefined.get(s) + "\n");
		}

		for (String s : rank2) {
			bw2.write(s + " - " + fdist2Refined.get(s) + "\n");
		}

		for (String s : rank3) {
			bw3.write(s + " - " + fdist3Refined.get(s) + "\n");
		}

		for (String s : rank4) {
			bw4.write(s + " - " + fdist4Refined.get(s) + "\n");
		}

		for (String s : rank5) {
			bw5.write(s + " - " + fdist5Refined.get(s) + "\n");
		}



		bw1.close();
		fw1.close(); //	

		bw2.close();
		fw2.close(); //	

		bw3.close();
		fw3.close(); //	

		bw4.close();
		fw4.close(); //

		bw5.close();
		fw5.close(); //

		bwParsed.close();
			fwParsed.close(); 
		 */}

}

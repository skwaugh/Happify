import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.PerformanceMonitor;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.inference.TestUtils;



public class gratitude {

	static String file0 = "/processed_txt/singleSentWithoutGrat.txt";
	static String file1 = "/processed_txt/corrected.txt";
	static String file2 = "/processed_txt/singleSentenceMessages.txt";
	static String file3 = "/processed_txt/singleSentTagged.txt";
	static String file5 = "/processed_txt/singleSentChunked.txt";
	static String file6 = "/processed_txt/singleSentParsed.txt";
	static String file7 = "/processed_txt/sqlNew.txt";
	

	static String[] gratitudeWords = {"grateful", "greatful",
		"brighten", "brightened", "bright", "brighter",
		"thankful", "thank", "thanks", "thanked",
		"love", "lovely", "loves", "loving",
		"happy", "glad", "excited", "appreciate", "proud", "bless", "blessed"
		};  

	
	static List<String> gratitudeWordList = Arrays.asList(gratitudeWords);

	static HashMap<String,User> userMap;

	static Set<String> userSet;// = HashSet<String>()

	public static void main(String[] args) throws InvalidFormatException, IOException {

		loadUserMap();
		makeUserSet();
		System.out.println("User map loaded, User set constructed");
		getStats();
		HashMap<String, ArrayList<String>> oneGrams = findNgrams(1);
		System.out.println("one grams found");
		//significanceTest(oneGrams);
		
		Lexicon relationships = new Lexicon("relationships.txt", "relationships");
		
		relationships.categorySigTest(oneGrams, userMap);
		/*
		Lexicon active = new Lexicon("active.txt", "active");
		Lexicon arts = new Lexicon("arts.txt", "arts");
		Lexicon diseaseAndMedicine = new Lexicon("diseaseAndMedicine.txt", "disease/medicine");
		Lexicon drugsAndAlcohol = new Lexicon("drugsAndAlcohol.txt", "drugs/Alcohol");
		Lexicon financial = new Lexicon("financial.txt", "financial");
		Lexicon friends = new Lexicon("friendRelationships.txt", "friends");
		Lexicon offspring = new Lexicon("offspringRelationships.txt", "offspring");
		Lexicon parental = new Lexicon("parentalRelationships.txt", "parental");
		Lexicon religion = new Lexicon("religionAndSpirituality.txt", "religion/spirituality");
		Lexicon siblings = new Lexicon("siblings.txt", "siblings");
		Lexicon significantOther = new Lexicon("significantOther.txt", "significant other");
		Lexicon work = new Lexicon("work.txt", "work");
		
		
		
		active.categorySigTest(oneGrams, userMap);
		arts.categorySigTest(oneGrams, userMap);
		diseaseAndMedicine.categorySigTest(oneGrams, userMap);
		drugsAndAlcohol.categorySigTest(oneGrams, userMap);
		financial.categorySigTest(oneGrams, userMap);
		friends.categorySigTest(oneGrams, userMap);
		offspring.categorySigTest(oneGrams, userMap);
		parental.categorySigTest(oneGrams, userMap);
		religion.categorySigTest(oneGrams, userMap);
		siblings.categorySigTest(oneGrams, userMap);
		significantOther.categorySigTest(oneGrams, userMap);
		work.categorySigTest(oneGrams, userMap);*/
	}

	
	/**
	 * creates a global HashSet<String> containing the  
	 * @throws FileNotFoundException
	 */
	public static void makeUserSet() throws FileNotFoundException {
		userSet = new HashSet<String>();
		File data = new File(file7);
		Scanner in = new Scanner(data);

		do {
			String[] line = in.nextLine().split("\t");
			//System.out.println();
			//String[] message = line[3].split(" ");
			//System.out.println(line[0]);
			userSet.add(line[0]);
			if(line[1].equals("527455")) return;
		} while(in.hasNextLine());	
		//System.out.println(userSet.size());
	}
	
	
	/**
	 * Using the userID's and corresponding scores in happinessScores_avg.txt, 
	 * loadUserMap() creates a HashMap of String userid --> User details for easy lookup
	 * @throws FileNotFoundException
	 */
	public static void loadUserMap() throws FileNotFoundException {
		File fileIn = new File("/Users/skwaugh/Desktop/happinessScores_avg.txt");
		Scanner in = new Scanner(fileIn);
		userMap = new HashMap<String,User>();
		in.nextLine(); //eat the column names
		do {
			String[] fields = in.nextLine().split("\t");
			if (!fields[2].equals("0")) {
				userMap.put(fields[1], new User(fields));
			}
		} while (in.hasNextLine());
	}
	
	/**
	 * removeGratStatements() parses a file of sentences and removes the "Gratitude clause"
	 * @throws IOException
	 */
	public static void removeGratStatements() throws IOException {
		File fileIn = new File(file3);
		Scanner in = new Scanner(fileIn);
		File fileOut = new File(file0);

		if (!fileOut.exists()) {
			fileOut.createNewFile();
		}
		FileWriter fw = new FileWriter(fileOut.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);


		//String input = "1._. I'm_PRP grateful_JJ for_IN my_PRP$ husband_NN who_WP kisses_VBZ me_PRP goodbye_NN every_DT day._NN";

		do {
			String input = in.nextLine();
			String[] inputArr = input.split(" ");
			String[] wordsArr = new String[inputArr.length];
			String[] tagsArr = new String[inputArr.length];
			int i = 0;
			for (String s : inputArr) {
				String[] split = s.split("_");
				wordsArr[i] = split[0];
				tagsArr[i++] = split[1];
			}

			i = 0;	
			if (wordsArr.length==1) {
				//if (wordsArr[0].substring(0,1).equals("1")) {
				//	wordsArr[0] = "\n";
				//}
			}
			else {
				if (wordsArr[0].substring(0,1).equals("1")) {
					wordsArr = removeWord(wordsArr, 0);
					tagsArr = removeWord(tagsArr, 0);
				}

				if (wordsArr[0].compareToIgnoreCase("i")==0 ||
						wordsArr[0].compareToIgnoreCase("im")==0 ||
						wordsArr[0].compareToIgnoreCase("i'am")==0 ||
						wordsArr[0].compareToIgnoreCase("i'm")==0) 
				{
					wordsArr = removeWord(wordsArr, 0);
					tagsArr = removeWord(tagsArr, 0);
				}

				if (wordsArr[0].compareToIgnoreCase("am")==0 || 
						wordsArr[0].compareToIgnoreCase("m")==0) {
					wordsArr = removeWord(wordsArr, 0);
					tagsArr = removeWord(tagsArr, 0);
				}
				
				

				if (gratitudeWordList.contains(wordsArr[0])&& tagsArr.length>1) {
					wordsArr = removeWord(wordsArr, 0);
					tagsArr = removeWord(tagsArr, 0);

				}

				if (tagsArr[0].equals("IN") && tagsArr.length>1) {  // if the first word is a preposition
					wordsArr = removeWord(wordsArr, 0);
					tagsArr = removeWord(tagsArr, 0);			
				}
				
				if (tagsArr[0].equals("TO") && tagsArr.length>1) {  // if the first word is the article "to"
					wordsArr = removeWord(wordsArr, 0);
					tagsArr = removeWord(tagsArr, 0);			
				}
				
				if (tagsArr[0].equals("PRP$") && tagsArr.length>1) {  // if the first word is a personal pronoun
					wordsArr = removeWord(wordsArr, 0);
					tagsArr = removeWord(tagsArr, 0);			
				}
				
				if (tagsArr[0].equals("POS") && tagsArr.length>1) {  // if the first word is a possesive prounoun
					wordsArr = removeWord(wordsArr, 0);
					tagsArr = removeWord(tagsArr, 0);			
				}
			}



			for(int index = 0; index<inputArr.length;index++) {

			}

			for (String tag : tagsArr) {
				System.out.print(tag+"\t");
			}
			System.out.println();
			for (String word : wordsArr) {
				System.out.print(word+"\t");
				bw.write(word+" ");
			}
			bw.write("\n");
			System.out.println();
		} while (in.hasNextLine());





		/*while((input=in.nextLine())!=null) {


		}*/



	}

	
	
	/**
	 * a helper method for removeGratStatements()
	 * removes a word from a gratitude statement
	 * @param arr - gratitude statement as a String array
	 * @param index - index of the word which needs to be removed
	 * @return a String array containing the original statement without the word indicated by the index
	 */
	public static String[] removeWord(String[] arr, int index) {
		String[] arrReturn = new String[arr.length-1];
		for (int i = 0; i<arrReturn.length;i++) {
			if (i<index)arrReturn[i] = arr[i];
			else arrReturn[i] = arr[i+1];
		}
		return arrReturn;
	}


	
	/**
	 * takes in a file of gratitude statements
	 * outputs a file gratitude statements that have been parsed using OpenNLP tools
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public static void parse() throws InvalidFormatException, IOException {
		// http://sourceforge.net/apps/mediawiki/opennlp/index.php?title=Parser#Training_Tool
		File fileIn = new File(file0);
		Scanner in = new Scanner(fileIn);

		File fileOut = new File(file6);

		if (!fileOut.exists()) {
			fileOut.createNewFile();
		}
		FileWriter fw = new FileWriter(fileOut.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		InputStream is = new FileInputStream("en-parser-chunking.bin");

		ParserModel model = new ParserModel(is);

		opennlp.tools.parser.Parser parser = (opennlp.tools.parser.Parser) ParserFactory.create(model);


		String sentence;
		while((sentence=in.nextLine())!=null) {

			Parse topParses[] = ParserTool.parseLine(sentence, (opennlp.tools.parser.Parser) parser, 1);

			for (Parse p : topParses) {
				StringBuffer x = new StringBuffer();
				//bw.write(p.toString());
				p.show(x);
				bw.write(x + "\n");
			}	
		}
		is.close();


	}

	/**
	 * takes in a file of gratitude statements
	 * outputs a file gratitude statements that have been "chunked" using OpenNLP tools
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public static void chunk() throws IOException {
		File fileIn = new File(file2);
		Scanner in = new Scanner(fileIn);
		File fileOut = new File(file5);

		if (!fileOut.exists()) {
			fileOut.createNewFile();
		}
		FileWriter fw = new FileWriter(fileOut.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		POSModel model = new POSModelLoader().load(new File("en-pos-maxent.bin"));
		PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "sent");
		POSTaggerME tagger = new POSTaggerME(model);

		String input;
		perfMon.start();
		try {
			while((input=in.nextLine())!=null) {
				ObjectStream<String> lineStream = new PlainTextByLineStream(new StringReader(input));
				String line = lineStream.read();
				String whitespaceTokenizerLine[] = WhitespaceTokenizer.INSTANCE
						.tokenize(line);
				String[] tags = tagger.tag(whitespaceTokenizerLine);
				do {
					whitespaceTokenizerLine = WhitespaceTokenizer.INSTANCE
							.tokenize(line);
					tags = tagger.tag(whitespaceTokenizerLine);

					POSSample sentence = new POSSample(whitespaceTokenizerLine, tags);
					System.out.println(sentence.toString());
					//bw.write(sentence+"\n");
					perfMon.incrementCounter();
				} while ((line = lineStream.read()) != null);
				//perfMon.stopAndPrintFinalResult();
				// chunker
				InputStream is = new FileInputStream("en-chunker.bin");
				ChunkerModel cModel = new ChunkerModel(is);

				ChunkerME chunkerME = new ChunkerME(cModel);

				/*String result[] = chunkerME.chunk(whitespaceTokenizerLine, tags);

			for (String s : result)
				System.out.println(s);

				Span[] span = chunkerME.chunkAsSpans(whitespaceTokenizerLine, tags);
				for (Span s : span)
					System.out.println(s.toString());
				//System.out.println("------------------");*/
			}
		}
		catch (NoSuchElementException n) {

		}

	}


	/**
	 * takes in a file of gratitude statements
	 * outputs a file gratitude statements that have been POS tagged using OpenNLP tools
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public static void posTag() throws IOException {
		File file = new File(file2);
		Scanner in = new Scanner(file);

		POSModel model = new POSModelLoader().load(new File("en-pos-maxent.bin"));
		PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "sent");
		POSTaggerME tagger = new POSTaggerME(model);

		File out = new File(file3);
		if (!out.exists()) {
			out.createNewFile();
		}
		FileWriter fw = new FileWriter(out.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);


		String input;
		perfMon.start();
		try {
			while((input=in.nextLine())!=null) {
				ObjectStream<String> lineStream = new PlainTextByLineStream(new StringReader(input));
				String line;
				while ((line = lineStream.read()) != null) {
					String whitespaceTokenizerLine[] = WhitespaceTokenizer.INSTANCE
							.tokenize(line);
					String[] tags = tagger.tag(whitespaceTokenizerLine);

					POSSample sentence = new POSSample(whitespaceTokenizerLine, tags);
					//System.out.println(sentence.toString());
					bw.write(sentence+"\n");
					perfMon.incrementCounter();
				}
				perfMon.stopAndPrintFinalResult();
			}
		}
		catch (NoSuchElementException n) {

		}
		//is.close();
	}



	/**
	 * Sentence Detect evaluates each message, splits the message into each sentence.  
	 * 
	 */
	public static void SentenceDetect(Line[] lines) throws InvalidFormatException,
	IOException {

		File out = new File(file2);
		if (!out.exists()) {
			out.createNewFile();
		}
		FileWriter fw = new FileWriter(out.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		// always start with a model, a model is learned from training data
		String[] sentences = new String[lines.length];
		//System.out.println();
		int max = 0;
		InputStream is = new FileInputStream("en-sent.bin");
		SentenceModel model = new SentenceModel(is);
		SentenceDetectorME sdetector = new SentenceDetectorME(model);
		for (Line l : lines) {
			max = 0;
			sentences = sdetector.sentDetect(l.short_text_1);
			if (sentences.length > 1) {// if there is >1 sentence, 
				int[] scores = new int[sentences.length];
				int i = 0;
				for (String sentence : sentences) {
					int get = getGratitudeWordCount(sentence);
					//scores[i++] = getGratitudeWordCount(sentence);
					scores[i] = get;
					//System.out.println(scores[i]);
				}
				for (int score : scores) { // for every score, the index of the score corresponds to index of sentence
					max = max > score ? max : score;
				}			
			}
			max = 0;
			String sentence = sentences[max];
			System.out.println(sentence);
			bw.write(sentence+"\n");
		}
		is.close();
	}
	/**picks the sentence that has the highest probability of being the main gratitude statement
	 * this is achieved through counting the number of explicit "gratitude" words & phrases used
	 * these words and phrases can be found in ngram files.
	 * 
	 * uses tokenizer
	 * @throws IOException 
	 * @throws InvalidFormatException 
	 */
	public static  int getGratitudeWordCount(String sentence) throws InvalidFormatException, IOException {
		int count = 0;

		InputStream is = new FileInputStream("en-token.bin");

		TokenizerModel model = new TokenizerModel(is);

		Tokenizer tokenizer = new TokenizerME(model);

		String tokens[] = tokenizer.tokenize(sentence);


		// what i want to do here is see how many words in this particular sentence appear in all of the other sentences
		// but should I be seeking similarity in structure as well?
		// this is trivial for now, will add to the library
		for (String token : tokens) {
			token = token.toLowerCase();
			if (gratitudeWordList.contains(token))
				count++;
		}

		is.close();
		return count;
	}


	/** 
	 *  takes in 2 files, one containing original data from happify, the other with cleaned up grat statements
	 *  appends the cleaned up grat statements to user id, message id, scores ,etc
	 * @throws IOException
	 */
	public static void assocIDwithMessage() throws IOException {
		File corrected = new File("/Users/skwaugh/Desktop/corrected2.txt");
		File noGrat = new File(file0);
		Scanner correctedScan = new Scanner(corrected);
		Scanner noGratScan = new Scanner(noGrat);

		File fileOut = new File(file7);

		if (!fileOut.exists()) {
			fileOut.createNewFile();
		}
		FileWriter fw = new FileWriter(fileOut.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);


		String[] sql = correctedScan.nextLine().split("\t");
		String message = noGratScan.nextLine();
		message = message.substring(0, message.length() - 1);
		int i=0;
		do {
			System.out.println("----" + message + "------" + sql[4] + "----" );
			if(sql[4].contains("  ")) {
				message = noGratScan.nextLine();

			}
			else if(sql[4].contains(message)) {
				//System.out.println("yeah!");
				bw.write(sql[0] + "\t" + sql[1] + "\t" + sql[2] + "\t" + message + "\n");
				message = noGratScan.nextLine();
				message = message.substring(0, message.length() - 1);
				i=0;
			}
			sql = correctedScan.nextLine().split("\t");
			i++;
			if (i>10) break;
		} while (correctedScan.hasNextLine());

	}


	/** 
	 * used to clean up the messy data from Happify
	 * @throws IOException
	 */
	public static void correct() throws IOException {
		File corrected = new File(file1);
		Scanner correctedScan = new Scanner(corrected);

		File fileOut = new File("/Users/skwaugh/Desktop/corrected2.txt");

		if (!fileOut.exists()) {
			fileOut.createNewFile();
		}
		FileWriter fw = new FileWriter(fileOut.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		do {
			String[] sql = correctedScan.nextLine().split("\t");
			String[] mess1arr = sql[4].split(" ");

			String mess1 = "";
			for (String m : mess1arr) {
				m = m.trim();
				System.out.println("-" + m + "-");
				mess1 += m + " ";
			}
			sql[4] = mess1;
			String row = "";
			for (String s : sql) {
				row += s + "\t";
			}
			bw.write(row + "\n");
			System.out.println(row);
		} while (correctedScan.hasNextLine());		

	}


	/**
	 * finds Ngrams, taking in a file with lines of the form "messageid, user id, date, gratSTatement"
	 * @param n
	 * @return
	 * @throws FileNotFoundException
	 */
	public static HashMap<String, ArrayList<String>> findNgrams(int n) throws FileNotFoundException {
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();

		File data = new File(file7);
		Scanner in = new Scanner(data);

		do {
			String[] line = in.nextLine().split("\t");
			String[] message = line[3].split(" ");
			for (int j = 0; j<(message.length-n+1); j++) {
				ArrayList<String> list = new ArrayList<String>();
				String ngram = "";
				for (int k=0; k<n; k++) {
					ngram += (message[j+k] + " ").toLowerCase();
				}
				//ngram = ngram.substring(0, ngram.length()-1); //remove trailing whitespace
				ngram = ngram.replaceAll("\\s", ""); // remove white space
				ngram = ngram.replaceAll("\\W", "");  // remove punctuation
				ngram = ngram.replaceAll("\\d", "");  //remove digits
				//if(!ngram.equals("")) {  // should I check if it is empty?
				if(map.containsKey(ngram)) {
					list = map.get(ngram);
				} 
				list.add(line[0]);
				map.put(ngram, list);
			}
		} while(in.hasNextLine());	
		/*
		for (Entry<String, List<String>> pair : map.entrySet()) {
			System.out.println
		}

		 */

		return map;
	}
	/**
	 * iterate over a dictionary
	 * find mean, stdev for each word
	 * find mean, stdev for whole group
	 * perform signifcance test on the group
	 */
	//public static void 


	/**
	 * finds the average User's score for a particular Ngram
	 */
	public static double findAverageScore(int scoreType, List<String> userIDlist) {
		int sum = 0;
		double nonNullCount = 0;
		double average = 0.0;

		for (String s : userIDlist) {
			//System.out.println(s);
			User user = userMap.get(s);
			if (user!=null) {
				nonNullCount++;
				switch (scoreType) {
				case 1: //happiness_score
					sum += Integer.parseInt(user.happiness_score);
					break;
				case 2: //positivity score
					sum += Integer.parseInt(user.positive_score);
					break;
				case 3: //satisfaction score
					sum += Integer.parseInt(user.satisfaction_score);
					break;
				}
			}
		}
		average = sum / nonNullCount;
		//avg = sum / userIDlist.size();
		return average;
	}

	public static void printNgrams(HashMap<String,List<String>> map) {
		for (Entry<String, List<String>> pair : map.entrySet()) {
			System.out.println(pair.getKey() + " - " + pair.getValue().size());
		}
	}
	/**
	 * returns an array of the ngrams in order of frequency
	 * @param map
	 * @return
	 * @throws IOException
	 */
	public static String[] rankByFrequency(HashMap<String,List<String>> map) throws IOException   {
		File fileOut = new File("/Users/skwaugh/Desktop/5gramScores.txt");
		if (!fileOut.exists()) {
			fileOut.createNewFile();
		}
		FileWriter fw = new FileWriter(fileOut.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		String[] nGramsRanked = new String[map.size()];
		boolean first = true;
		//int counter = 0;
		for (Map.Entry<String,List<String>> pair : map.entrySet()) {
			int i = 0;
			//String key = pair.getKey();
			if(first==true) {
				nGramsRanked[i] = pair.getKey();
				first = false;
				//System.out.println("entered if in FreqDist rankbyFrequ...");
			}
			else {
				int value = pair.getValue().size();

				while (map.get(nGramsRanked[i])!=null) {
					//System.out.println(value + " - " + fdistRefined.get(oneGramsRanked[i]));
					if (value>map.get(nGramsRanked[i]).size())
						break;
					i++;	
				}
				String temp = nGramsRanked[i];
				String next;
				nGramsRanked[i++] = pair.getKey();
				for(int j=i; j<map.size(); j++) {
					next = temp;
					temp = nGramsRanked[j];
					nGramsRanked[j] = next;
				}
			}
			//counter++;
		}
		//int x = 0;
		//System.out.println
		fw.write("ngram" + "\t" + "#" + "\t" + "hap" + "\t" + "pos" + "\t" + "sat" + "\n");
		for (String s : nGramsRanked) {
			double avgHappy = findAverageScore(1, map.get(s));
			double avgPos = findAverageScore(2, map.get(s));
			double avgSat = findAverageScore(3, map.get(s));
			//System.out.println
			fw.write(s + "\t" + map.get(s).size() + "\t " +
					avgHappy + "\t " + avgPos + "\t " + avgSat + "\n");
		}
		return nGramsRanked;
	}

	/**
	 * takes in an ngram mapping, removes those who do not occur a particular # of times
	 * @param occurs
	 * @param oneGrams
	 * @return
	 */

	public static HashMap<String, ArrayList<String>> refineByFrequency(int occurs, HashMap<String, ArrayList<String>> oneGrams) {
		HashMap<String, ArrayList<String>> mapRefined = new HashMap<String, ArrayList<String>>();

		for (Entry<String, ArrayList<String>> pair : oneGrams.entrySet()) {
			if (pair.getValue().size()>=occurs) {
				//System.out.println(pair.getKey() + " - " + pair.getValue());
				mapRefined.put(pair.getKey(),pair.getValue());
			}
		}
		return mapRefined;
	}

	/**
	 * takes in HashMap<String, ArrayList<String>>, which maps an ngram to a list of Happify users who mentioned it
	 * outputs a file that contains the significance tests for each n gram
	 * @param oneGrams
	 * @throws IOException
	 */
	public static void significanceTest(HashMap<String, ArrayList<String>> oneGrams) throws IOException {

		File fileOut = new File("/processed_txt/1gramsWithSignificanceTests.txt");
		if (!fileOut.exists()) {
			fileOut.createNewFile();
		}
		FileWriter fw = new FileWriter(fileOut.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		fw.write("ngram" + "\t" + "t" + "\t" + "p" + "\t" + "#members" + "\t" + "meanM" 
				+ "\t" + "StdevM" + "\t" + "#nonMembers" + "\t" + "meanN" + "\t" + "StDevN" + "\n");	

		for (Entry<String, ArrayList<String>> e : refineByFrequency(10, oneGrams).entrySet()) {

			List<String> membersOriginal = e.getValue();
			List<String> members = new ArrayList<String>(membersOriginal);

			for (String user : membersOriginal) {
				if (userMap.get(user)==null) {
					members.remove(user);
				}
			}



			List<String> nonmembers = new ArrayList<String>(userMap.keySet());

			for (String user : members) {
				nonmembers.remove(user);
			}

			double[] memberScores = getScores(members);
			double[] nonMemberScores = getScores(nonmembers);

			double t = TestUtils.t(memberScores, nonMemberScores);
			double p = TestUtils.tTest(memberScores, nonMemberScores);
			double meanM = StatUtils.mean(memberScores);
			double hapVarM = StatUtils.variance(memberScores);
			double hapStdevM = Math.sqrt(hapVarM);
			double meanN = StatUtils.mean(nonMemberScores);
			double hapVarN = StatUtils.variance(nonMemberScores);
			double hapStdevN = Math.sqrt(hapVarN);
			/*
			System.out.println(e.getKey() + "\t" + t + "\t" + p);
			System.out.println("Member - " + meanM + ", " + hapStdevM);
			System.out.println("nonMember - " + meanN + ", " + hapStdevN);
			System.out.println("# members = " + memberScores.length);
			System.out.println("# nonmembers = " + nonMemberScores.length);
			System.out.println();*/

			String ngram = e.getKey();
			if(!ngram.equals("\"i")) {
				fw.write(ngram + "\t" + t + "\t" + p + "\t" + memberScores.length + "\t" + meanM
						+ "\t" + hapStdevM + "\t" + nonMemberScores.length + "\t" + meanN + "\t" + hapStdevN + "\n");
			}

		}



	}

	/**
	 * returns an array of the happiness scores of the people who mentioned a particular ngram
	 * @param members
	 * @return
	 */
	public static double[] getScores(List<String> members) {
		double[] scores = new double[members.size()];

		int i = 0;
		int nullCount = 0, total = 0;
		for(String member : members) {
			try {
				//if (member!=null) {
				double score = (double) Integer.parseInt(userMap.get(member).happiness_score); // must update to incorporate type
				scores[i++] = score;
				//}
			} catch(NullPointerException n) {
				nullCount++;
				//System.out.print(member + ", ");
			}
			total++;
		}
		//System.out.println(nullCount + "/" + total);
		//if (nullCount>0) System.out.println(members.toString());
		return scores;
	}


	/**
	 * finds stats for the sample, output can be found under "stats" in analysis.xlsx excel file
	 */
	public static void getStats() {

		ArrayList<Double> hapList = new ArrayList<Double>();
		ArrayList<Double> posList = new ArrayList<Double>();
		ArrayList<Double> satList = new ArrayList<Double>();

		for (String userID : userSet) {
			User user = userMap.get(userID);
			if (user!=null) {
				//System.out.println(userID);
				hapList.add((double) Integer.parseInt(user.happiness_score));
				posList.add((double) Integer.parseInt(user.positive_score));
				satList.add((double) Integer.parseInt(user.satisfaction_score));
			}
		}


		double[] hap = doubleListToArray(hapList);
		double[] pos = doubleListToArray(posList);
		double[] sat = doubleListToArray(satList);

		double hapMean = StatUtils.mean(hap);
		double[] hapMode = StatUtils.mode(hap);
		double hapVar = StatUtils.variance(hap);
		double hapStdev = Math.sqrt(hapVar);
		double hapMax = StatUtils.max(hap);
		double hapMin = StatUtils.min(hap);

		double posMean = StatUtils.mean(pos);
		double[] posMode = StatUtils.mode(pos);
		double posVar = StatUtils.variance(pos);
		double posStdev = Math.sqrt(posVar);
		double posMax = StatUtils.max(pos);
		double posMin = StatUtils.min(pos);

		double satMean = StatUtils.mean(sat);
		double[] satMode = StatUtils.mode(sat);
		double satVar = StatUtils.variance(sat);
		double satStdev = Math.sqrt(satVar);
		double satMax = StatUtils.max(sat);
		double satMin = StatUtils.min(sat);



		System.out.println("\t" + "hap" + "\t" + "pos" + "\t" + "sat");
		System.out.println("mean" + "\t" + hapMean + "\t" + posMean + "\t" + satMean);
		System.out.println("mode" + "\t" + hapMode[0] + "\t" + posMode[0] + "\t" + satMode[0]);
		System.out.println("stdev" + "\t" + hapStdev + "\t" + posStdev + "\t" + satStdev);
		System.out.println("var" + "\t" + hapVar + "\t" + posVar + "\t" + satVar);
		System.out.println("max" + "\t" + hapMax + "\t" + posMax + "\t" + satMax);
		System.out.println("min" + "\t" + hapMin + "\t" + posMin + "\t" + satMin);

		//System.out.println(hapZeroCount);

		//System.out.println(posZeroCount);

		//System.out.println(satZeroCount);
		//System.out.println(userMap.size());
	}


	/**
	 * helper method to get around java's inability to use primitives with toArray
	 * @param list
	 * @return
	 */
	public static double[] doubleListToArray(List<Double> list) {
		double[] arr = new double[list.size()];

		int index = -1;
		for (double d : list) {
			arr[++index] = d;
		}


		return arr;
	}







}

/* disregard this
public static void oldMain() throws InvalidFormatException, IOException {
	File file = new File(file1);
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
	SentenceDetect(lines);
}*/
























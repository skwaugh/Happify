import java.awt.List;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.inference.TestUtils;


public class Lexicon {

	HashSet<String> dictionary;
	File file;
	String category;


	public Lexicon(String filename, String c) {
		category = c;
		file = new File(filename);
		dictionary = new HashSet<String>();
		try {
			Scanner in = new Scanner(file);
			while (in.hasNextLine()) {
				String next = in.nextLine();
				//System.out.println(next);
				dictionary.add(next);
			}
				//dictionary.add(in.next());
		} catch (FileNotFoundException e) {
			System.out.println("File not found.  Try something else.");
		}

	}

	/**
	 * adds a word to THIS lexica; writes it to the file
	 * returns true if word was written to file
	 * returns false if not written or error
	 */
	public boolean addWord(String word) {
		FileWriter fw;
		try {
			this.dictionary.add(word);
			fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("/n"+word);
			bw.close();
			fw.close();
		} catch (IOException e) {
			return false;		
		}
		return true;
	}

	/**
	 * adds an entire lexica to this Lexica
	 * e.g. add a lexica about paternal relationships to lexica about relationships
	 * returns true if lexica was written to file
	 * returns false if not written or error
	 */
	public boolean addLexica(Lexicon l) {
		for (String s : l.dictionary) {
			addWord(s);
		}
		return true;
	}

	public void categorySigTest(HashMap<String, ArrayList<String>> ngrams, 
			HashMap<String,User> userMap) { 

		//HashSet<String> membersOriginal = new HashSet<String>(); 
		ArrayList<String> membersOriginal = new ArrayList<String>();

		for (String ngram : this.dictionary) {
			if (ngrams.get(ngram)!=null) {
				membersOriginal.addAll(ngrams.get(ngram));
			}
		}
		ArrayList<String> members = new ArrayList<String>(membersOriginal);
		for (String member : membersOriginal) {
			if (userMap.get(member)==null) {
				members.remove(member);
			}
		}

		ArrayList<String> nonmembers = new ArrayList<String>(userMap.keySet());

		for (String member : members) {
			nonmembers.remove(member);
		}

		double[] memberScores = gratitude.getScores(members);
		double[] nonMemberScores = gratitude.getScores(nonmembers);

		double t = TestUtils.t(memberScores, nonMemberScores);
		double p = TestUtils.tTest(memberScores, nonMemberScores);
		double meanM = StatUtils.mean(memberScores);
		double hapVarM = StatUtils.variance(memberScores);
		double hapStdevM = Math.sqrt(hapVarM);
		double meanN = StatUtils.mean(nonMemberScores);
		double hapVarN = StatUtils.variance(nonMemberScores);
		double hapStdevN = Math.sqrt(hapVarN);

		System.out.println(category + "\t" + t + "\t" + p + "\t" + memberScores.length + "\t" + meanM
				+ "\t" + hapStdevM + "\t" + nonMemberScores.length + "\t" + meanN + "\t" + hapStdevN);

	}

	/*
	 * methods
	 * addWord - add words to the lexicon
	 * addLexica - add a sub-lexica to an existing lexica
	 * read - display lexicon
	 * 
	 * 	// tool for managing lexica to compare the objects of gratitude
	 */


}

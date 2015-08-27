import java.io.*;
import java.util.*;


public class FreqDist {

	File file = null;
	HashMap<String,Integer> fdist;
	HashMap<String,Integer> fdist2;
	HashMap<String,Integer> fdist3;
	HashMap<String,Integer> fdist4;
	HashMap<String,Integer> fdist5;
	HashMap<String,Integer> fdist6;


	//HashMap<String,Integer> fdistRefined;
	Collection<String> ignoreDictionary;
	
	

	public FreqDist(String filename) throws FileNotFoundException {
		file = new File(filename);
		fdist = fdist();
	}

	public FreqDist(Line[] lines) {
		fdist = fdist(lines);
		//fdist2 = nGramFdist(lines, 2);
		//fdist3 = nGramFdist(lines, 3);
		//fdist4 = nGramFdist(lines, 4);
		//fdist5 = nGramFdist(lines, 5);


	}


	
	
	public HashMap<String,Integer> fdist() throws FileNotFoundException {
		HashMap<String,Integer> freqDist = new HashMap<String,Integer>();
		Scanner in = new Scanner(file);
		while(in.hasNext()) {
			int count = 0;
			String next = in.next();
			if(freqDist.containsKey(next)) {
				count = freqDist.get(next);				
			}
			freqDist.put(next, ++count);
		}		
		return freqDist;	
	}

	public HashMap<String,Integer> fdist(Line[] lines) {
		HashMap<String,Integer> freqDist = new HashMap<String,Integer>();
		int i = 0;
		for (Line line : lines) {
			//System.out.println(i);
			if (i<49196) {  // to avoid null pointer except - quick fix
				String input = (line.short_text_1);// + " " + line.short_text_2 + " " + line.short_text_3) ;
				Scanner in = new Scanner(input);
				while(in.hasNext()) {
					int count = 0;
					String next = in.next();
					if(freqDist.containsKey(next)) {
						count = freqDist.get(next);				
					}
					freqDist.put(next, ++count);
				}
			}
			i++;
		}
		return freqDist;
	}

	public HashMap<String,Integer> nGramFdist(Line[] lines, int n) {
		HashMap<String,Integer> nGramFreqDist = new HashMap<String,Integer>();
		int i = 0;
		for (Line line : lines) {
			if (i<49196) {  // to avoid null pointer except - quick fix
				String input = (line.short_text_1);// + " " + line.short_text_2 + " " + line.short_text_3) ;
				//Scanner in = new Scanner(input);
				String[] arrStrings = input.split(" ");
				for (int j = 0; j<(arrStrings.length-n+1); j++) { //starting word	
					int count = 0;
					String ngram = "";
					for (int k=0; k<n; k++) {
						ngram += (arrStrings[j+k] + " ");
					}
					ngram = ngram.substring(0, ngram.length()-1);
					if(nGramFreqDist.containsKey(ngram)) {
						count = nGramFreqDist.get(ngram);
					}
					nGramFreqDist.put(ngram, ++count);
				}
			}
			i++;
		}		
		return nGramFreqDist;		
	}


	/**
	 * 
	 * @param word - 
	 * @return count - raw count of the word in the file
	 */
	public int count(String word) {
		return fdist.get(word);
	}


	/**
	 * 
	 * @param word
	 * @return freq - probability of the word in the file
	 * "returns the unsmoothed maximum likelihood estimate"
	 */
	public float freq(String word) {

		float total = 0;
		Collection<Integer> counts = fdist.values();
		for (int x : counts) total += x;
		System.out.println(total);
		return count(word) / total;
	}

	/**
	 * removes words from the hashmap that do not occur a particular number of times
	 * @param occurs
	 */
	public static HashMap<String,Integer> refineByFrequency(int occurs, HashMap<String,Integer> fdist) {
		HashMap<String,Integer> fdistRefined = new HashMap<String,Integer>();

		for (Map.Entry<String, Integer> pair : fdist.entrySet()) {
			if (pair.getValue()>=occurs) {
				//System.out.println(pair.getKey() + " - " + pair.getValue());
				fdistRefined.put(pair.getKey(),pair.getValue());
			}
		}
		return fdistRefined;
	}


	public String[] rankByFrequency(HashMap<String,Integer> fdistRefined)   {
		
		String[] nGramsRanked = new String[fdistRefined.size()];
		boolean first = true;
		//int counter = 0;
		for (Map.Entry<String, Integer> pair : fdistRefined.entrySet()) {
			int i = 0;
			//String key = pair.getKey();
			if(first==true) {
				nGramsRanked[i] = pair.getKey();
				first = false;
				//System.out.println("entered if in FreqDist rankbyFrequ...");
			}
			else {
				int value = pair.getValue();
				while (fdistRefined.get(nGramsRanked[i])!=null) {
					//System.out.println(value + " - " + fdistRefined.get(oneGramsRanked[i]));
					if (value>fdistRefined.get(nGramsRanked[i]))
						break;
					i++;	
				}
				String temp = nGramsRanked[i];
				String next;
				nGramsRanked[i++] = pair.getKey();
				for(int j=i; j<fdistRefined.size(); j++) {
					next = temp;
					temp = nGramsRanked[j];
					nGramsRanked[j] = next;
				}
			}
			//counter++;
		}
		//int x = 0;
		for (String s : nGramsRanked) {
			//System.out.println(s + " - " + fdistRefined.get(s));
		}
		return nGramsRanked;
	}


	/**remove words belonging to a particular dictionary
	 * articles
	 * 
	 */
	public void removeWords() {


	}


	public void removePunctuation() {


	}



	/*public static void main(String[] args) throws FileNotFoundException {
		FreqDist FD = new FreqDist("austen.token");
		System.out.println(FD.fdist.toString());
		System.out.println(FD.count("profession"));
		System.out.println(FD.freq("profession"));

	}*/

}


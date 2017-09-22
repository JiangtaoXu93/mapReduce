import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.Map.Entry;

public class Subtask01 {

	private static final int K_NEIGHBORHOOD = 2;
	private static List<String> CORPUS;

	public static void main(String[] args) throws IOException {
		long d1 = System.nanoTime();
		File folder = new File("input");
		File[] listOfFiles = folder.listFiles();// get all files
		Map<Character, Integer> letterScores = getLetterScore(listOfFiles);// get letter score
		Map<String, Integer> wordScores = getwordScore(letterScores);// get each word score

		Map<String, Double> kNeighborhoodsScores = getkNeighborhoodsScores(wordScores);// get letter frequency score

		printResult(kNeighborhoodsScores);// print the result

		long d2 = System.nanoTime();
		System.out.println((d2 - d1) / 1000000);

	}

// *********************************************************************************************************//
// *********************************************************************************************************//
// *********************************************************************************************************//
	// print the result
	public static void printResult(Map<String, Double> kNeighborhoodsScores) throws IOException {
		// sort map of kNeighborhoodsScores
		Set<Entry<String, Double>> s = kNeighborhoodsScores.entrySet();
		List<Entry<String, Double>> l = new ArrayList<Entry<String, Double>>(s);
		Collections.sort(l, new Comparator<Map.Entry<String, Double>>() {
			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});

		PrintWriter printwriter = new PrintWriter(new FileOutputStream("result01.csv"));

		for (Map.Entry<String, Double> entry : l) {
			printwriter.print(entry.getKey());
			printwriter.print(",");
			printwriter.print(entry.getValue());
			printwriter.print("\n");

		}
		printwriter.close();
	}

// *********************************************************************************************************//
// *********************************************************************************************************//
// *********************************************************************************************************//
	// get letter scores from file

	public static Map<Character, Integer> getLetterScore(File[] listOfFiles) throws IOException {

		// *********************************************************************************************************//
		// count letters from file
		int totalLetters = 0;// count the whole letter of the corpus
		StringBuilder corpus = new StringBuilder();// store the whole corpus of 100 files
		Map<Character, Integer> letterFrequency = new HashMap<Character, Integer>();// store the letter frequency

		for (int i = 0; i < listOfFiles.length; i++) {// read each file

			FileReader fr = new FileReader(listOfFiles[i].getPath());
			BufferedReader br = new BufferedReader(fr);
			corpus.append("START ");// add START to label the start of a paragraph
			String line = null;// each line
			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty()) {// replace empty line with 2 words: "START" "END"
					corpus.append(" END ");// add END to label the start of a paragraph
					corpus.append(" START ");
				} else {
					line = line.toLowerCase().replaceAll("[^a-zA-Z ]", "");// remove punctuations and numbers
					corpus.append(line);// add words to corpus

					for (int j = 0; j < line.length(); j++) {
						Character c = line.charAt(j);
						if (Character.isLetter(c)) {
							c = Character.toLowerCase(c);
							totalLetters++;
							Integer count = letterFrequency.get(c);
							letterFrequency.put(c, (count == null) ? 1 : count + 1);// add letter to map
						}
					}

				}
			}
			corpus.append(" END ");// add END to label the end of a text
			br.close();
		}
		corpus.append(" END");
// *********************************************************************************************************//
		// get letter scores
		Map<Character, Integer> letterScore = new HashMap<Character, Integer>();

		for (Character ch : letterFrequency.keySet()) {
			double chCount = (double) letterFrequency.get(ch);
			if (chCount / totalLetters >= 0.1)
				letterScore.put(ch, 0);
			else if (chCount / totalLetters >= 0.08)
				letterScore.put(ch, 1);
			else if (chCount / totalLetters >= 0.06)
				letterScore.put(ch, 2);
			else if (chCount / totalLetters >= 0.04)
				letterScore.put(ch, 4);
			else if (chCount / totalLetters >= 0.02)
				letterScore.put(ch, 8);
			else if (chCount / totalLetters >= 0.01)
				letterScore.put(ch, 16);
			else
				letterScore.put(ch, 32);
		}

// *********************************************************************************************************//
		// set corpus
		CORPUS = new ArrayList<String>(Arrays.asList(corpus.toString().split(" ")));// transfer BuilderString to
																					// ArrayList

		return letterScore;
	}

// *********************************************************************************************************//
// *********************************************************************************************************//
// *********************************************************************************************************//
	// get word scores
	public static Map<String, Integer> getwordScore(Map<Character, Integer> letterScores) {
		Map<String, Integer> wordScore = new HashMap<String, Integer>();
		ListIterator<String> i = CORPUS.listIterator();
		while (i.hasNext()) {// get word score by adding the letter scores
			String word = i.next();
			int letterCount = 0;
			for (int j = 0; j < word.length(); j++) {
				Character c = word.charAt(j);
				Integer count = letterScores.get(c);
				letterCount = letterCount + ((count == null) ? 0 : count);
			}
			wordScore.put(word, letterCount);

		}
		return wordScore;
	}

// *********************************************************************************************************//
// *********************************************************************************************************//
// *********************************************************************************************************//
	// get letter scores from file

	public static Map<String, Double> getkNeighborhoodsScores(Map<String, Integer> wordScores) {
		Map<String, List<Integer>> kScores = new HashMap<String, List<Integer>>();
		// word and its all k-neighbor score

		for (int i = 1; i < CORPUS.size(); i++) {
			String word = CORPUS.get(i);

			if (!word.equals("START") && !word.equals("END")) {// skip when word is START or END
				String[] neighbors = new String[K_NEIGHBORHOOD * 2];
				int count = 1;
				while (!CORPUS.get(i - count).equals("START")) {// add front neighbor to array
					if (count > K_NEIGHBORHOOD)
						break;
					neighbors[count - 1] = CORPUS.get(i - count);
					count++;
				}
				count = 1;
				while (!CORPUS.get(i + count).equals("END")) {// add back neighbor to array
					if (count > K_NEIGHBORHOOD)
						break;
					neighbors[K_NEIGHBORHOOD + count - 1] = CORPUS.get(i + count);
					count++;
				}

				int score = countSingleWordNeighbor(neighbors, wordScores);// calculate score of its neighbors
				List<Integer> countList = kScores.get(word);
				if (countList == null) {
					countList = new ArrayList<Integer>();
				}
				countList.add(score);// append new score to word
				kScores.put(word, countList);
			}
		}

		Map<String, Double> kNeighborhoodsScores = new HashMap<String, Double>();

		for (String w : kScores.keySet()) {// calculate the mean score of neighbors
			List<Integer> countList = kScores.get(w);
			double total = 0;
			for (int i = 0; i < countList.size(); i++) {
				total = countList.get(i) + total;
			}

			kNeighborhoodsScores.put(w, total / countList.size());
		}

		return kNeighborhoodsScores;
	}

// *********************************************************************************************************//
// *********************************************************************************************************//
// *********************************************************************************************************//
	// given the array of neighbors calculate the score
	public static int countSingleWordNeighbor(String[] neighbors, Map<String, Integer> wordScores) {
		int score = 0;
		for (int i = 0; i < neighbors.length; i++) {
			if (neighbors[i] != null) {
				score = score + wordScores.get(neighbors[i]);
			}
		}

		return score;
	}

}

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.Map.Entry;



public class Subtask02 {
	
	private static final int K_NEIGHBORHOOD = 2;
	private static int NT;// number of threads
	private static final int DEFAULT_NT = 8;
	
	static class ReadFromFile{//store the corpus and letter frequency, corpus count from file
		List<String> corpus;// corpus of file
		Map<Character, Integer> letterFrequency;//letter frequency of file
		int totalLetters;// letter count of file
	}
	
	
	static class MyReadRunnable implements Runnable {
		List<File> listOfFiles;//files to read
		ReadFromFile readResult;// result of reading
		MyReadRunnable(List<File> listOfFiles) throws IOException {
			this.listOfFiles = listOfFiles;
		}

		@Override
		public void run() {
			try {
				readResult = getLetterFrequency(listOfFiles);// get letter frequency from files
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
	}
	
	
	static class MyCalculateRunnable implements Runnable {
		List<String> corpus;//corpus to calculate
		Map<Character, Integer> letterScore;//score of each letter
		Map<String, List<Integer>> NeighborhoodsScores;//the result of k neighbor
		MyCalculateRunnable(List<String> corpus, Map<Character, Integer> letterScore){
			this.corpus = corpus;
			this.letterScore = letterScore;
		}

		@Override
		public void run() {
			Map<String, Integer> wordScores =  getwordScore(letterScore, corpus);
			 //get word scores from each corpus
			NeighborhoodsScores = getkNeighborhoodsScores(wordScores, corpus);
			 // get k neighbor score from each corpus
			
		}
	}
	
	
//*********************************************************************************************************//
//*********************************************************************************************************//
//*********************************************************************************************************//
//main function	
	
	public static void main(String[] args) throws IOException {
		long d1 = System.nanoTime();
		NT = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_NT;
		File folder = new File("input");
        List<File> listOfFiles = new ArrayList<File>(Arrays.asList(folder.listFiles()));// get all files

//*********************************************************************************************************//
// using concurrency to read letter frequency from files        
        List<Thread> threads = new ArrayList<Thread>();
        List<MyReadRunnable> tasks = new ArrayList<MyReadRunnable>();        
        int segment = (listOfFiles.size()) / NT;
        int from = 0;
        
        for (int i = 0; i < NT; i++) {
        	MyReadRunnable task = new MyReadRunnable(listOfFiles.subList(from, from + segment));
          tasks.add(task);
          from += segment;
          Thread worker = new Thread(task);
          worker.start();
          threads.add(worker);
        }
        int running = 0;
        do {
          running = 0;
          for (Thread thread : threads) {
            if (thread.isAlive()) running++;
          }
        } while (running > 0);
//*********************************************************************************************************//
     // merge the result from threads
        

		@SuppressWarnings("rawtypes")
		List[] corpus = new List[NT];
		Map<Character, Integer> letterFrequency = new HashMap<Character, Integer>();
		int totalLetters = 0;
		int i = 0;
		
		for (MyReadRunnable task : tasks) {//merge the letter frequency from tasks
			corpus[i] = task.readResult.corpus;
			i++;
			totalLetters = totalLetters + task.readResult.totalLetters;
			for (Character c : task.readResult.letterFrequency.keySet()) {
				Integer subCount = task.readResult.letterFrequency.get(c);
				Integer count = letterFrequency.get(c);
				letterFrequency.put(c, count == null ? subCount : count + subCount);				
			}

		}
		Map<Character, Integer> letterScore = new HashMap<Character, Integer>();
		for (Character ch : letterFrequency.keySet()) {//according to the frequency, calculate the score of each
			 											//letter
			double chCount = (double) letterFrequency.get(ch);
			if (chCount / totalLetters >= 0.1) letterScore.put(ch, 0);
			else if (chCount / totalLetters >= 0.08)letterScore.put(ch, 1);
			else if (chCount / totalLetters >= 0.06)letterScore.put(ch, 2);
			else if (chCount / totalLetters >= 0.04)letterScore.put(ch, 4);
			else if (chCount / totalLetters >= 0.02)letterScore.put(ch, 8);
			else if (chCount / totalLetters >= 0.01)letterScore.put(ch, 16);
			else letterScore.put(ch, 32);
		}
		
		
//*********************************************************************************************************//
		// using concurrency to calculate the k neighbor of each corpus
		
		
		List<Thread> calThreads = new ArrayList<Thread>();
		List<MyCalculateRunnable> calTasks = new ArrayList<MyCalculateRunnable>();


		for (i = 0; i < NT; i++) {
			@SuppressWarnings("unchecked")
			MyCalculateRunnable task = new MyCalculateRunnable(corpus[i],letterScore);
			calTasks.add(task);
			Thread worker = new Thread(task);
			worker.start();
			calThreads.add(worker);
		}
		running = 0;
		do {
			running = 0;
			for (Thread thread : calThreads) {
				if (thread.isAlive()) running++;
			}
		} while (running > 0);
		
//*********************************************************************************************************//
		// merge the result from threads 
		Map<String, List<Integer>> kScores = new HashMap<String, List<Integer>>();
		
		for (MyCalculateRunnable task : calTasks) {//for each task, get all scores of one word
			Map<String, List<Integer>> NeighborhoodsScores = task.NeighborhoodsScores;
			for (String w : NeighborhoodsScores.keySet()) {
				List<Integer> subScores = NeighborhoodsScores.get(w);
				List<Integer> scores = kScores.get(w);
				if (scores == null) {
					scores = new ArrayList<Integer>();
				}
				scores.addAll(subScores);
				kScores.put(w, scores);
			}
		}
		
		Map<String, Double> kNeighborhoodsScores = new HashMap<String, Double>();
		for (String w : kScores.keySet()) {// for each word, calculate the mean score
			List<Integer> countList = kScores.get(w);
			double total = 0;
			for (i = 0; i < countList.size(); i++) {
				total = countList.get(i) + total;
			}			
			kNeighborhoodsScores.put(w, total / countList.size());
		}

//*********************************************************************************************************//
		// print result		
		printResult(kNeighborhoodsScores);
        
        long d2 = System.nanoTime();
        System.out.println((d2 - d1) / 1000000);
		
	}
	
	
	
//*********************************************************************************************************//
//*********************************************************************************************************//
//*********************************************************************************************************//
//print the result
	public static void printResult(Map<String, Double> kNeighborhoodsScores) throws IOException {
		// sort map of kNeighborhoodsScores
		Set<Entry<String,Double>> s = kNeighborhoodsScores.entrySet();
		List<Entry<String,Double>> l = new ArrayList<Entry<String,Double>>(s);
		Collections.sort(l, 
				new Comparator<Map.Entry<String, Double>>() {
			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});//sort the map

		PrintWriter printwriter = new PrintWriter(new FileOutputStream("result02.csv"));

		for ( Map.Entry<String, Double> entry : l) {
			printwriter.print(entry.getKey());
			printwriter.print(",");
			printwriter.print(entry.getValue());
			printwriter.print("\n");

		}
		printwriter.close();
	}
	
	
//*********************************************************************************************************//
//*********************************************************************************************************//
//*********************************************************************************************************//
//get letter scores from file	
	
	public static ReadFromFile getLetterFrequency(List<File> listOfFiles) throws IOException{
		
//*********************************************************************************************************//
//count letters from file
		int totalLetters = 0;// count the whole letter of the corpus
		StringBuilder corpus = new StringBuilder();//store the whole corpus of 100 files
		Map<Character, Integer> letterFrequency = new HashMap<Character, Integer>();//store the letter frequency
		
        for (int i = 0; i < listOfFiles.size(); i++){//read each file
        		
            FileReader fr = new FileReader(listOfFiles.get(i).getPath());
            BufferedReader br = new BufferedReader(fr);
            corpus.append("START ");//add START to label the start of a paragraph
            String line = null;//each line 
            while ((line = br.readLine()) != null){
            		if (line.trim().isEmpty()) {//replace empty line with 2 words: "START" "END"
            			corpus.append(" END ");//add END to label the start of a paragraph
            			corpus.append(" START ");
            		}
            		else{
            			line = line.toLowerCase().replaceAll("[^a-zA-Z ]", "");// remove punctuations and numbers 
            			corpus.append(line);// add words to corpus
            			
                		for(int j = 0; j < line.length(); j++) {
                			Character c = line.charAt(j);
                			if(Character.isLetter(c)) {
                				c = Character.toLowerCase(c);
                				totalLetters++;
                				Integer count = letterFrequency.get(c);
                				letterFrequency.put(c, (count == null) ? 1:count + 1);// add letter to map
                			}
                		}
                	
                }
                }
			corpus.append(" END ");//add END to label the end of a text
            br.close();     
            }
        corpus.append(" END");
        
//*********************************************************************************************************//
//set corpus
        ReadFromFile readResult = new ReadFromFile();
        readResult.letterFrequency = letterFrequency;
        readResult.totalLetters = totalLetters;
        
        readResult.corpus = new ArrayList<String>(
                Arrays.asList(corpus.toString().split(" ")));//transfer BuilderString to ArrayList        

        return readResult;
        }
	
	
//*********************************************************************************************************//
//*********************************************************************************************************//
//*********************************************************************************************************//
//get word scores 
	public static Map<String, Integer> getwordScore(Map<Character, Integer> letterScores, List<String> corpus){
		Map<String, Integer> wordScore = new HashMap<String, Integer>();
		ListIterator<String> i = corpus.listIterator();
		while(i.hasNext()) {
			String word = i.next();
			int letterCount = 0;
			for (int j = 0; j < word.length();j++) {
				Character c = word.charAt(j);
				Integer count = letterScores.get(c);
				letterCount = letterCount + ((count == null) ? 0: count);
			}
			wordScore.put(word, letterCount);
			
		}
		return wordScore;
	}

	
	
	
//*********************************************************************************************************//
//*********************************************************************************************************//
//*********************************************************************************************************//
//get letter scores from file	
	
	public static Map<String, List<Integer>> getkNeighborhoodsScores(Map<String, Integer> wordScores, List<String> corpus){
		Map<String, List<Integer>> kScores = new HashMap<String, List<Integer>>();
		 //word and its all k-neighbor score
			
		for (int i = 1; i < corpus.size(); i++) {
			String word = corpus.get(i);
			
			if (!word.equals("START") && !word.equals("END")) {//skip when word is START or END
				String[] neighbors = new String[K_NEIGHBORHOOD * 2];
				int count = 1;
				while (!corpus.get(i - count).equals("START")) {//add front neighbor to array
					if (count > K_NEIGHBORHOOD) break;
					neighbors[count - 1] = corpus.get(i - count);
					count++;
				}
				count = 1;
				while (!corpus.get(i + count).equals("END")) {//add back neighbor to array
					if (count > K_NEIGHBORHOOD) break;
					neighbors[K_NEIGHBORHOOD + count - 1] = corpus.get(i + count);
					count++;
				}
				
				int score = countSingleWordNeighbor(neighbors,wordScores);//calculate score of its neighbors			
				List<Integer> countList = kScores.get(word);
				if (countList == null) {
					countList = new ArrayList<Integer>();
				}
				countList.add(score);// append new score to word
				kScores.put(word, countList);
			}
		}
		
		return kScores;
	}
	
//*********************************************************************************************************//
//*********************************************************************************************************//
//*********************************************************************************************************//
//given the array of neighbors calculate the score		
	public static int countSingleWordNeighbor(String[] neighbors,Map<String, Integer> wordScores) {
		int score = 0;
		for (int i = 0; i < neighbors.length; i++) {
			if (neighbors[i] != null) {
				score = score + wordScores.get(neighbors[i]);
			}
		}
		
		return score;
	}
	

}

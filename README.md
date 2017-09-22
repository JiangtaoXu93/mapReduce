There are 2 java programs: Subtask01.java is a sequential program,Subtask02.java is a parallel/concurrent program.

In Subtask01: 
Use getLetterScore() function to get letter score from 100 files; after attain letter score, using getwordScore() to calculate each word score; then using getkNeighborhoodsScores() to calculate the neighbothood score; finally using printResult() to print result to result01.csv.

In Subtask02: 
There are 2 classes implements Runable class: MyReadRunnable class read words from 100 files and calculate letter frequency parallelly; MyCalculateRunnable class read words from memory, count neighborhood scores.

In main function: there are two parts of merge: the one is merging the letter frequency from NT cluster(NT is number of thread), then come out the letter scores; the other is merging the neighborhood score from NT cluster, then calculate the mean score of the k-neihborhood.

How to run:
In Subtask01, at line 12: 	private static final int K_NEIGHBORHOOD = 2;
you can change the value of k, without input, compile and then run the code.

In Subtask02, you need to input number of thread when running the code, otherwise,change DEFAULT_NT at line 16 to control the number of threads:
private static final int DEFAULT_NT = 8;

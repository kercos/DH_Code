package corpora;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import util.Utility;

public class MADRE_ExploreKeywordsVariations {

	static PrintStream out = System.out;

	static TreeMap<Integer, HashSet<java.util.List<String>>> keywordsLength;
	static int totalSentences, selectedSenteces;
	
	static void init() {
		keywordsLength = new TreeMap<Integer, HashSet<java.util.List<String>>>();
		totalSentences = 0; 
		selectedSenteces = 0;
	}

	static void readKeywordFile(File keywordFile) throws FileNotFoundException, IOException {
		out.println("Read keywords from file: " + keywordFile);
		Scanner scan = new Scanner(keywordFile);
		int totKw = 0;
		while(scan.hasNextLine()) {			
			String kw = scan.nextLine().toLowerCase();
			if (!kw.isEmpty()) {
				LinkedList<String> kwList = new LinkedList<String>(Arrays.asList(kw.split("\\s+")));
				Utility.putInTreeMapSet(keywordsLength, kwList.size(), kwList);
				totKw++;
			}
		}
		out.println("Read keywords: " + totKw);
		scan.close();
	}

	static void processCsvFile(File fileCsv) throws FileNotFoundException, IOException {
		out.println("Processing file: " + fileCsv);
		
		CSVParser parser=null;
		try {
			parser = CSVParser.parse(fileCsv, Charset.forName("UTF-8"), CSVFormat.RFC4180); //CSVFormat.EXCEL //CSVFormat.MYSQL
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		LinkedList<TextToken> token_sentence = new LinkedList<TextToken>();
		int sentenceCount = -1;
		
		Iterator<CSVRecord> lineIter = parser.iterator();
		lineIter.next(); // header
						
		while(lineIter.hasNext()) {
			CSVRecord line = lineIter.next();
			String word = MADRE_TextPro_Analysis.removeQuotes(line.get(4));
			String pos = MADRE_TextPro_Analysis.removeQuotes(line.get(10));
			String lemma = MADRE_TextPro_Analysis.removeQuotes(line.get(13));
			//if (correctLemmas.containsKey(lemma))
			//	lemma = correctLemmas.get(lemma);
			
			int sentenceId = Integer.parseInt(MADRE_TextPro_Analysis.removeQuotes(line.get(3)));
			if (sentenceCount!=sentenceId) {
				totalSentences++;
				sentenceCount = sentenceId;
				if (!token_sentence.isEmpty()) {
					//do something
					token_sentence.clear();		
				}
			}
			Character posGroup = MADRE_TextPro_Analysis.resolvePosGroup(pos);
			TextToken token = new TextToken(word, lemma, posGroup);
			token_sentence.add(token);			
		}		
	}

	/*
	static void processFileWindow(File file, int word_window_size) throws FileNotFoundException, IOException {
		out.println("Processing file: " + file);
		InputStream gzipStream = new GZIPInputStream(new FileInputStream(file));
		@SuppressWarnings("resource")
		BufferedReader br = new BufferedReader(new InputStreamReader(gzipStream));
		 		 
		LinkedList<String> word_window = new LinkedList<String>();
		
		br.readLine(); // heading line
		
		HashBasedTable<String, String, int[]> auxiliaryMatrix = HashBasedTable.create();
		// coMatrix[w1][w2] contains the number of times w2 follows w1 (withing a window size of words)
		
		String line = null;
		while( (line = br.readLine()) != null) {
			String[] split = line.split("\t");
			String pos = removeQuotes(split[10]);
			String lemma = removeQuotes(split[13]);
			if (excludedLemmas.contains(lemma))
				continue;
			//String lemma_pos = lemma + '_' + pos;
			Character posGroup = getPosGroup(pos);
			if (posGroup!=null) {
				Utility.increaseInHashMap(lemma_freq, lemma);
				Utility.increaseInHashMap(pos_freq, pos);
				String lemmaPosGroup = lemma + "_" + posGroup;
				Utility.increaseInHashMap(labels_freq, lemmaPosGroup);
				
				word_window.add(lemmaPosGroup);
				if (word_window.size()<word_window_size)
					continue;
				ListIterator<String> iterFirst = word_window.listIterator();
				ListIterator<String> iterSecond = null;
				String first = null, second = null;
				while(iterFirst.hasNext()) {			
					first = iterFirst.next();
					iterSecond = word_window.listIterator(iterFirst.nextIndex());
					while(iterSecond.hasNext()) {
						second = iterSecond.next();
						increaseInMatrix(first, second, auxiliaryMatrix);				
					}
				}
				if (word_window.size()>word_window_size)
					word_window.removeFirst();
			}
		}
		
		//build final matrix
		out.println("Building Co-Matrix Simmetric");		
		ArrayList<String> lemmaPosListFiltered = new ArrayList<String>(labels_freq.keySet());		
		ListIterator<String> iterFirst = lemmaPosListFiltered.listIterator();
		ListIterator<String> iterSecond = null;
		String first = null, second = null;
		while(iterFirst.hasNext()) {			
			first = iterFirst.next();
			iterSecond = lemmaPosListFiltered.listIterator(iterFirst.nextIndex());
			while(iterSecond.hasNext()) {
				second = iterSecond.next();
				if (first.compareTo(second)>=0)
					continue;
				int value = 0;
				int[] v1 = auxiliaryMatrix.get(first, second);
				int[] v2 = auxiliaryMatrix.get(second, first);
				if (v1!=null)
					value += v1[0];
				if (v2!=null)
					value += v2[0];
				if (value!=0)
					finalMatrix.put(first, second, new int[]{value});				
			}
		}
		
	}
	*/
	
	
	
	/*
	0  	 ID  	 Token counter, starting at 1 for each new sentence.
	1  	 FORM  	 Word form or punctuation symbol. 		
	2 	LEMMA 	Lemma or stem (depending on particular data set) of word form, or an underscore if not available.
	3 	CPOSTAG 	Coarse-grained part-of-speech tag, where tagset depends on the language.
	4 	POSTAG 	Fine-grained part-of-speech tag, where the tagset depends on the language, or identical to the coarse-grained part-of-speech tag if not available.
	5 	FEATS 	Unordered set of syntactic and/or morphological features (depending on the particular language), separated by a vertical bar (|), or an underscore if not available.
	6 	HEAD 	Head of the current token, which is either a value of ID or zero ('0'). Note that depending on the original treebank annotation, there may be multiple tokens with an ID of zero.
	7 	DEPREL 	Dependency relation to the HEAD. The set of dependency relations depends on the particular language. Note that depending on the original treebank annotation, the dependency relation may be meaningfull or simply 'ROOT'.
	8 	PHEAD 	Projective head of current token, which is either a value of ID or zero ('0'), or an underscore if not available. Note that depending on the original treebank annotation, there may be multiple tokens an with ID of zero. The dependency structure resulting from the PHEAD column is guaranteed to be projective (but is not available for all languages), whereas the structures resulting from the HEAD column will be non-projective for some sentences of some languages (but is always available).
	9 	PDEPREL Dependency relation to the PHEAD, or an underscore if not available. The set of dependency relations depends on the particular language. Note that depending on the original treebank annotation, the dependency relation may be meaningfull or simply 'ROOT'. 
	*/
	
	/*
	static void processFileDep(File fileConll) throws FileNotFoundException, IOException {
		out.println("Processing file: " + fileConll);
		Scanner scan = new Scanner(fileConll);
		 		 
		//HashMultimap<String,String>
		ConnlX sentenceConnl = null;
		while( (sentenceConnl = ConnlX.getNextConnlLinesSentence(scan)) != null) {
			for(int i=0; i<sentenceConnl.length; i++) {
				String lemma = sentenceConnl.lemmas[i];
				String pos = sentenceConnl.cpostags[i];
				Utility.increaseInHashMap(lemma_freq, lemma);
				Utility.increaseInHashMap(pos_freq, pos);
				//String lemma_pos = lemma + '_' + pos;				
			}
			
			TDNode[] treeStructure = TDNode.getTreeStructure(
					sentenceConnl.forms,
					sentenceConnl.lemmas,
					sentenceConnl.cpostags, 
					sentenceConnl.deprels, 
					sentenceConnl.heads, 
					1, //baseIndex 
					0  //rootIndex
			);
			
			ArrayList<TDNode> roots = TDNode.getRoots(treeStructure);			
			for(TDNode r : roots) {
				extractDepLinks(r);
			}
		}
		
	}
	
	static String[] depRelSbjObj = new String[]{"SUBJ", "DOBJ"};
	
	private static void extractDepLinks(TDNode p) {
		boolean rootVerb = p.postag.charAt(0)=='V' && !p.lemma.equals("essere");				
		for(TDNode[] daughters : p.daughters()) {
			if (daughters!=null) {
				for(TDNode d: daughters) {
					if (rootVerb && d.postag.charAt(0)=='S') {
						for (String depRel : depRelSbjObj) { // SUBJ, DOBJ
							if (d.deprel.equals(depRel)) {							
								String noun = d.lemma + "_" + d.postag.charAt(0) + "_" + depRel;
								String verb = p.lemma + "_" + p.postag.charAt(0);
								increaseInMatrix(noun, verb, finalMatrix);
								Utility.increaseInHashMap(labels_freq, noun);
								Utility.increaseInHashMap(labels_freq, verb);
								for(TDNode[] nephews : d.daughters()) {
									if (nephews!=null) {
										for(TDNode n: nephews) {
											if (n.postag.charAt(0)=='A') {
												String adj = n.lemma + "_" + n.postag.charAt(0);
												increaseInMatrix(noun, adj, finalMatrix);
												Utility.increaseInHashMap(labels_freq, adj);
												Utility.increaseInHashMap(labels_freq, noun);
											}
										}
									}
								}
								break;
							}
						}						
					}					
					extractDepLinks(d);
				}
			}
		}
		
	}
	*/
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		String path = "/Volumes/HardDisk/Scratch/Projects/MADRE/";
		File keywordFile = new File(path + "patterns.txt");
		File inputFile = new File(path + "Corpora/trentino_stampa_2011/trentino_stampa_2011.csv");
		
		init();
		readKeywordFile(keywordFile);
		processCsvFile(inputFile);
	}

}

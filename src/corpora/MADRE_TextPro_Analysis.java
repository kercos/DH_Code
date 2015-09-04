package corpora;
import it.uniroma1.dis.wsngroup.gexf4j.core.Edge;
import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import it.uniroma1.dis.wsngroup.gexf4j.core.Graph;
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode;
import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.Attribute;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeClass;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeList;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.data.AttributeListImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.viz.ColorImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.viz.Color;
import it.uniroma1.dis.wsngroup.gexf4j.core.viz.NodeShape;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.graphstream.algorithm.BetweennessCentrality;
import org.graphstream.algorithm.BetweennessCentrality.Progress;
import org.graphstream.graph.implementations.SingleGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.GraphMLExporter;
import org.jgrapht.ext.IntegerEdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.StringEdgeNameProvider;
import org.jgrapht.ext.StringNameProvider;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.UndirectedWeightedSubgraph;
import org.xml.sax.SAXException;

import util.FileUtil;
import util.Utility;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Table.Cell;

/*
	FIELDS:
	
	0: "id"
	1: "corpus_id"
	2: "file_name"
	3: "sentence_id"
	4: "token"
	5: "tokennorm"
	6: "tokenid"
	7: "tokenstart"
	8: "tokenend"
	9: "tokentype"
	10: "pos"
	11: "full_morpho"
	12: "comp_morpho"
	13: "lemma"
	14: "entity"
	15: "chunk"
	16: "geoinfo"
	17: "parserid"
	18: "feats"
	19: "head"
	20: "deprel"
	21: "file_id"
	22: "year"
*/
	
	
/*
	All TextPro pos tags
	
	XPS		punctuation							.;:?!
	XPW		comma								,
	XPB		brakets								()
	XPO		quotation mark, ellipsis, hyphen	`` '' - ... _
	N		number								1, 1999, '76, sei, sesto
	RS		singular article					il, l', la, un, una
	RP		plural article						i, gli, gl', le
x	AS		singular qual. adj.					vera, grandissimo, migliore
x	AP		plural qual. adj.					vere, grandissimi, maggiori
x	AN		qual. adj. neutral for number		rosa, più, super, antincendio
	DS		singular det. adj.					quello, alcuna, mio, quale?
	DP		plural det. adj.					quelli, alcune, miei, quali?
	DN		det. adj. neutral for number		qualsiasi
	E		simple preposition					di, a, dopo, fino, nonostante
	ES		singular articulated preposition	dal, sulla, nello
	EP		plural articulated preposition		dalle, sulle, negli, nei, ai
	B		adverb								molto, invece, esattamente
	C		conjunction							e, ma, bensı, sia, perché
	CCHE	che									che
	CCHI	chi									chi
	CADV	connettivo avverbiale				come, dove, quando
	PS		singular pronoun					ciascuna, lo, mio
	PP		plural pronoun						costoro, esse, nostri, loro
	PN		pronoun neutral for number			ci, cui, sé
x	SS		singular noun						aereo, formula
x	SP		plural noun							aerei, formule
x	SN		noun neutral for number				attività, business, novità
x	SPN		proper noun							Alfredo, Ford, Piombino
	QNS		singular relative pronoun			quanto, quanta
	QNP		plural relative pronoun				quanti, quante
x	YA		acronym								ANSA, CEE, ONU
x	YF		foreign term						city, fiesta, Papier
	I		interjection						oh!
x	VI		main verb, ind., subjunctive, cond.	vedo, giungano, saprei
	VIY		aux. verb, ind., subjunctive, cond.	ho, sia, avrebbe
x	VF		main verb, inf.						arrivare, vedere
	VFY		aux. verb, inf.						avere, essere
x	VSP		main verb, past part., singular		acquisito, interrotto
	VSPY	aux. verb, past part., singular		avente, stato, stata
x	VPP		main verb, past part., plural		arrivati
	VPPY	aux. verb, past part., plural		state
x	VG		main verb, gerund					cantando, ringraziando
	VGY		aux. verb, gerund					avendo, essendo
x	VM		main verb, imperative				cercate, leggi
	VMY		aux. verb, imperative				sia, abbia
	+E		clitic	ne, ci

*/

public class MADRE_TextPro_Analysis {
	
	// coMatrix[w1][w2] exists if w1<w2 (in alphabetical order)
	// it contains the number of time w1 is in proximity of w2 (before or after a window size of words)
	
	//Selected pos tags (don't forget to remove +E, e.g., VF+E)
	final static String[] selectedPoS = new String[]{
		"AS",	//		singular qual. adj.					vera, grandissimo, migliore
		"AP",	//		plural qual. adj.					vere, grandissimi, maggiori
		"AN",	//		qual. adj. neutral for number		rosa, più, super, antincendio
		"SS", 	//		singular noun						aereo, formula
		"SP",	//		plural noun							aerei, formule
		"SN",	//		noun neutral for number				attività, business, novità
		"SPN",	//		proper noun							Alfredo, Ford, Piombino
		"YA",	//		acronym								ANSA, CEE, ONU
		"YF",	//		foreign term						city, fiesta, Papier
		"VI",	//		main verb, ind., subjunctive, cond.	vedo, giungano, saprei
		"VF",	//		main verb, inf.						arrivare, vedere
		"VSP",	//		main verb, past part., singular		acquisito, interrotto
		"VPP",	//		main verb, past part., plural		arrivati
		"VG",	//		main verb, gerund					cantando, ringraziando
		"VM",	//		main verb, imperative				cercate, leggi
	};
	
	static final char ADJECTIVE_POS = 'A';
	static final char KEYWORD_POS = 'K';
	static final char NOUN_POS = 'S';
	static final char VERB_POS = 'V';
		
	static final char[] posGroupLabels = new char[]{
		ADJECTIVE_POS, 	//'A'
		KEYWORD_POS,	//'K'
		NOUN_POS,		//'S'
		VERB_POS};		//'V'
	
	static final Color[] colorsPos = new ColorImpl[]{
			new ColorImpl(0,255,0), 	// green adj (A)
			new ColorImpl(255,0,255), 	// violet keywords (K)
			new ColorImpl(0,0,255), 	// blue nouns (S)
			new ColorImpl(255,0,0) 		// red verbs (V)			
	};
	
	static final Color blackColor = new ColorImpl(0,0,0);
	
	static final HashSet<String> excludedLemmas = new HashSet<String>(
			Arrays.asList(
					new String[]{
							"avere",
							"essere", 
							"essere stare", 
							"RIPRODUZIONE",
							"RISERVATA",
							"b",
							"c"
					}
					//new String[]{}
					// potere, volere,
				));
	
	static final HashMap<String, String> correctLemmas = new HashMap<String, String>();
	
	final static int maxSD = 5;

	static {
		Arrays.sort(selectedPoS);
		correctLemmas.put("tempio tempo", "tempo");		
		correctLemmas.put("bel", "bello");
		correctLemmas.put("belle", "bello");
		correctLemmas.put("buon", "buono");
		correctLemmas.put("gran", "grande");
		correctLemmas.put("maggior", "maggiore");
		correctLemmas.put("miglior", "migliore");
		correctLemmas.put("vario varo", "vario");
		correctLemmas.put("accordio accordo", "accordo");
		correctLemmas.put("Associazione", "associazione");
		correctLemmas.put("Aula", "aula");
		correctLemmas.put("Castel", "castello");
		correctLemmas.put("Castelli", "castello");
		correctLemmas.put("Castello", "castello");
		correctLemmas.put("Cda", "cda");
		correctLemmas.put("Comune", "comune");
		correctLemmas.put("lavorio lavoro", "lavoro");
		correctLemmas.put("lavoratrici", "lavoratrice");
		correctLemmas.put("legge leggio", "legge");
		correctLemmas.put("modio modo", "modo");
		correctLemmas.put("noma nome nomo", "nome");		
		correctLemmas.put("parte parto", "parte");
		correctLemmas.put("principe principio", "principio");
		correctLemmas.put("prof.", "professore");
		correctLemmas.put("professor", "professore");
		correctLemmas.put("salute saluto", "salute");
		correctLemmas.put("sedo sede", "sede");
		correctLemmas.put("sindaci", "sindaco");
		correctLemmas.put("valle vallo", "valle");
		correctLemmas.put("parlamentare parlamentario", "parlamentare");		
		correctLemmas.put("genere genero", "genere");
	}
	
	//static boolean pruneKeywords = false;
	static boolean useKeywords, nodeSizeBasedOnDegree;

	//FILEDS
	
	static HashMap<String, int[]> lemma_freq;
	static HashMap<Character, HashMap<String, int[]>> pos_lemma_freq;
	static HashMap<String, int[]> pos_freq;
	static HashMap<String, int[]> labels_freq;
	static HashMap<String, int[]> labels_degree;
	static HashMap<String, double[]> labels_degree_norm;	
	
	static HashBasedTable<String, String, int[]> finalMatrix;
	// coMatrix[w1][w2] exists if w1<w2 (in alphabetical order)
	// it contains the number of time w1 is in proximity of w2 (before or after a window size of words)
	
	static int totalSentences, selectedSentences;
	static DescriptiveStatistics nodeFreqStats, arcWeightsStats, nodeDegreeStats;
	static HashSet<String> foundKeyWords; 
	static HashSet<String> lemmasWithSpaces;
	
	static TreeMap<Integer, HashSet<java.util.List<String>>> keywordsLength; 
	
	static PrintStream out = System.out;
	static PrintWriter pw_selectedFlatLemma;
	//METHODS
	
	static void init() {
		lemma_freq = new HashMap<String, int[]>();
		pos_lemma_freq = new HashMap<Character, HashMap<String, int[]>>();
		pos_freq = new HashMap<String, int[]>();
		labels_freq = new HashMap<String, int[]>();	
		labels_degree_norm = new HashMap<String, double[]>();
		labels_degree = new HashMap<String, int[]>();
		finalMatrix = HashBasedTable.create();
		totalSentences=0;
		selectedSentences=0;
		//DescriptiveStatistics nodeFreqStats, arcWeightsStats;
		foundKeyWords = new HashSet<String>(); 		
		keywordsLength = new TreeMap<Integer, HashSet<java.util.List<String>>> ();
		lemmasWithSpaces = new HashSet<String>();

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
	
	static void convertFileToFlat(File fileCsv, File outputFile) throws FileNotFoundException, IOException {
		out.println("Converting input file to flat: " + outputFile);
		
		CSVParser parser=null;
		try {
			parser = CSVParser.parse(fileCsv, Charset.forName("UTF-8"), CSVFormat.RFC4180); //CSVFormat.EXCEL //CSVFormat.MYSQL
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		PrintWriter pw = new PrintWriter(outputFile);
		StringBuilder sb = new StringBuilder();

		int sentenceCount = -1;
		
		Iterator<CSVRecord> lineIter = parser.iterator();
		lineIter.next(); // header
						
		while(lineIter.hasNext()) {
			CSVRecord line = lineIter.next();
			String word = removeQuotes(line.get(4));
			int sentenceId = Integer.parseInt(removeQuotes(line.get(3)));
			if (sentenceCount!=sentenceId) {
				totalSentences++;
				sentenceCount = sentenceId;
				pw.println(sb.toString().trim());
				sb = new StringBuilder();
			}
			sb.append(' ').append(word);
		}		
		
		pw.close();
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
			String word = removeQuotes(line.get(4));
			String pos = removeQuotes(line.get(10));
			String lemma = removeQuotes(line.get(13));
			String correctLemma = correctLemmas.get(lemma);
			if (correctLemma!=null)
				lemma = correctLemma;
			
			int sentenceId = Integer.parseInt(removeQuotes(line.get(3)));
			if (sentenceCount!=sentenceId) {
				sentenceCount = sentenceId;
				dealWithSentence(token_sentence);				
			}
			Character posGroup = resolvePosGroup(pos);
			TextToken token = new TextToken(word, lemma, posGroup);
			token_sentence.add(token);			
		}		
		dealWithSentence(token_sentence);
	}
	
	private static void dealWithSentence(LinkedList<TextToken> token_sentence) {		
		if (!token_sentence.isEmpty()) {
			totalSentences++;		
			if (useKeywords) 
				processSentenceKeywords(token_sentence);
			else {
				selectedSentences++;
				processSentence(new LinkedList<TextToken>(token_sentence), token_sentence);
			}
			token_sentence.clear();		
		}		
	}

	private static void processSentenceKeywords(LinkedList<TextToken> token_sentence) {
		
		int foundKw = 0;
		
		int length = token_sentence.size();
		
		boolean[][] kwSpan = new boolean[length+1][length+1];
		// matrix with all found span, need to check possible crossing
		
		
		for(int l = 1; l<=length; l++) {
			for(int s=0; s<=length-l; s++) {
				int e = s + l;
				HashSet<java.util.List<String>> kwSet = keywordsLength.get(l);
				if (kwSet==null)
					continue;
				java.util.List<TextToken> span = token_sentence.subList(s, e);
				java.util.List<String> span_wordList = TextToken.getWordList(span, true);
				for(java.util.List<String> kw : kwSet) {
					if (kw.equals(span_wordList)) {
						kwSpan[s][e] = true;
						foundKw++;
						foundKeyWords.add(kw.toString());
						break;
					}
				}
			}
		}
		
		if (foundKw==0) {
			if (pw_selectedFlatLemma!=null) {
				pw_selectedFlatLemma.println("--- REJECTED ---");
				pw_selectedFlatLemma.println(TextToken.toStringWords(token_sentence, false));
				pw_selectedFlatLemma.println();
			}
			return;
		}
		
		if (hasConflicts(token_sentence, kwSpan))
			return;
		
		selectedSentences++;
		
		LinkedList<TextToken> finalToken_sentence = new LinkedList<TextToken>();
		
		int previousEnds = 0;
		for(int s=0; s<length; s++) {
			for(int e=s+1; e<length; e++) {				
				if (kwSpan[s][e]) {
					if (s!=previousEnds) {
						for(int i=previousEnds; i<s; i++) {
							finalToken_sentence.add(token_sentence.get(i));
							// add single words when there are holes between mwes
						}
					}
					java.util.List<TextToken> span = token_sentence.subList(s, e);
					String wk_words = TextToken.toStringWords(span, true);
					TextToken wk_token = new TextToken(wk_words, wk_words, KEYWORD_POS);
					finalToken_sentence.add(wk_token);
					previousEnds = e;
				}
			}
		}
		if (previousEnds!=length) {
			for(int i=previousEnds; i<length; i++) {
				finalToken_sentence.add(token_sentence.get(i));
				// add single words when there are holes between mwes
			}
		}
		
		processSentence(token_sentence, finalToken_sentence);
	}
	


	private static boolean hasConflicts(LinkedList<TextToken> token_sentence, boolean[][] kwSpan) {
				
		// remove overalapping (e.g., lavoro femminile, lavoro femminile volontario)
		removeOverlapping(kwSpan);
		
		int length = token_sentence.size();
		
		ArrayList<int[]> spans = new ArrayList<int[]>(); 
		for(int s=0; s<length; s++) {
			for(int e=s+1; e<length; e++) {
				if (kwSpan[s][e])
					spans.add(new int[]{s,e});
			}
		}	
		
		
		/*
		 *     A
		 *  _______
		 *  |     |
		 *     |________|
		 *          B
		 *          
		 *     B
		 *  _______
		 *  |     |
		 *     |________|
		 *          A         
		 */
		
		ListIterator<int[]> iterA = spans.listIterator();
		while(iterA.hasNext()) {
			int[] nextA = iterA.next();
			ListIterator<int[]> iterB = spans.listIterator(iterA.nextIndex());
			while(iterB.hasNext()) {
				int[] nextB = iterB.next();
				if ( (nextB[0]>nextA[0] && nextB[0]<nextA[1]) || 
					(nextA[0]>nextB[0] && nextA[1]<nextB[1])) {					
					System.err.println("Conflict in sentence:");
					System.err.println("SENTENCE: " + TextToken.toStringWords(token_sentence, false));
					System.err.println("KW_1: " + TextToken.toStringWords(token_sentence.subList(nextA[0], nextA[1]),false));
					System.err.println("KW_2: " + TextToken.toStringWords(token_sentence.subList(nextB[0], nextB[1]),false));
					return true;
				}
			}
		}
		
		return false;
	}

	private static void removeOverlapping(boolean[][] kwSpan) {

		// remove overalapping (e.g., lavoro femminile, lavoro femminile volontario)
		// span are like in indexes (start included, end excluded)
		int length = kwSpan.length;
		
		for(int s=0; s<length; s++) {
			for(int e=length-1; e>s; e--) {
				if (kwSpan[s][e]) {
					//remove all true in the bottom left square
					for(int i=s; i<length; i++) {
						for(int j=e; j>0; j--) {
							if (i==s && j==e || i>j) //upper right quadrant, esclude current cell
								continue;
							kwSpan[i][j] = false;
						}
					}
				}
			}
		}
		
	}
	
	private static void processSentence(LinkedList<TextToken> token_sentence, LinkedList<TextToken> finalToken_sentence) {
		
		ListIterator<TextToken> iterFirst = finalToken_sentence.listIterator();
		while(iterFirst.hasNext()) {
			TextToken next = iterFirst.next();
			if (!excludedLemmas.contains(next.lemma) && next.pos!=null) {
				if (next.lemma.contains(" ")) {
					if (lemmasWithSpaces.add(next.lemma)) {
						//out.println("\tWARNING: Found lemma with space: " + next.lemma);
						//System.err.println("WARNING: Found lemma with space: " + next.lemma);
					}
				}
				Utility.increaseInHashMap(lemma_freq, next.lemma);
				Utility.increaseInHashMap(pos_freq, next.pos.toString());
				Utility.increaseInHashMap(pos_lemma_freq, next.pos, next.lemma); 
				String lemmaPosGroup = next.lemmaPos;
				Utility.increaseInHashMap(labels_freq, lemmaPosGroup);			
			}
			else
				iterFirst.remove();
		}
		
		if (pw_selectedFlatLemma!=null) {
			//pw_selectedFlatLemma.println("--- ACCEPTED ---");
			pw_selectedFlatLemma.println(TextToken.toStringWords(token_sentence, false));
			pw_selectedFlatLemma.println(TextToken.toStringLemmasPos(finalToken_sentence, false));
			pw_selectedFlatLemma.println();
		}
				
		iterFirst = finalToken_sentence.listIterator();
		ListIterator<TextToken> iterSecond = null;
		String first = null, second = null;
		while(iterFirst.hasNext()) {			
			first = iterFirst.next().lemmaPos;
			iterSecond = finalToken_sentence.listIterator(iterFirst.nextIndex());
			while(iterSecond.hasNext()) {
				second = iterSecond.next().lemmaPos;
				int cmp = first.compareTo(second);
				if (cmp==0) // same lemmaPos
					continue;
				else if (cmp>0)
					increaseInMatrix(first, second, finalMatrix);
				else 
					increaseInMatrix(second, first, finalMatrix);
			}
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



	static String removeQuotes(String s) {
		if (s.length()>1 && s.startsWith("\"") && s.endsWith("\""))
			return s.substring(1, s.length()-1);
		return s;
	}
	
	static Character resolvePosGroup(String pos) {		
		if (pos.endsWith("+E")) {
			pos = pos.substring(0, pos.length()-2);
		}
		if (Arrays.binarySearch(selectedPoS, pos)>=0) {
			char p = pos.charAt(0);
			if (p=='Y') 
				p = 'S'; // acronyms and foreign words are like nouns
			return p;

		}
		return null;
	}

	private static void increaseInMatrix(String first, String second, HashBasedTable<String, String, int[]> matrix) {
		
		// avoid reflective links	
		if (first.equals(second))
			return;
		
		int[] count = matrix.get(first, second);
		if (count==null) {
			count = new int[]{0};
			matrix.put(first, second, count);
		}
		count[0]++;		
	}

	private static void computeStatistics() {
		out.println("\n----- COMPUTING STATISTICS -----");
		nodeFreqStats = getStats(labels_freq.values());		
		arcWeightsStats = getStats(finalMatrix.values());
		
		//out.println(nodeFreqStats);
		//out.println(arcWeightsStats);
	}
	
	private static DescriptiveStatistics getStats(Collection<int[]> values) {
		DescriptiveStatistics result = new DescriptiveStatistics();
		Iterator<int[]> iter = values.iterator();
		while(iter.hasNext()) {
			result.addValue(iter.next()[0]);
		}
		return result;
	}
	
	private static DescriptiveStatistics getStatsDouble(Collection<double[]> values) {
		DescriptiveStatistics result = new DescriptiveStatistics();
		Iterator<double[]> iter = values.iterator();
		while(iter.hasNext()) {
			result.addValue(iter.next()[0]);
		}
		return result;
	}

	private static void pruneMatrix(int nodeSDcutOff, int arcSDcutOff) {
		
		if (nodeSDcutOff<0 && arcSDcutOff<0) {
			return;
		}
		
		out.println("\n" + "------ PRUNING MATRIX -------");
		
		double nodeFreqThreshold = 0;
		double arcWeightsThreshold = 0;
		
		if (nodeSDcutOff==-1) 
			out.println("Prune mode nodes: freq > 0 (no pruning)");
		else {
			nodeFreqThreshold = nodeFreqStats.getMean() + nodeFreqStats.getStandardDeviation()*nodeSDcutOff;
			out.println("Prune mode nodes: freq > mean" + "+ " + nodeSDcutOff + "SD [" + nodeFreqThreshold + "]");
		}
		
		if (arcSDcutOff==-1)
			out.println("Prune mode arcs: weights > 0 (no pruning)");
		else {
			arcWeightsThreshold = arcWeightsStats.getMean() + arcWeightsStats.getStandardDeviation()*arcSDcutOff;
			out.println("Prune mode arcs:  weight > mean " + "+ " + arcSDcutOff + "SD [" + arcWeightsThreshold + "]");
		}
		 	
		// prune by label freq
		Iterator<Entry<String, int[]>> iterLabelsFreq = labels_freq.entrySet().iterator();
		Set<String> matrixRowKeys = finalMatrix.rowKeySet();
		Set<String> matrixColumnKeys = finalMatrix.columnKeySet();
		while(iterLabelsFreq.hasNext()) {
			Entry<String, int[]> next = iterLabelsFreq.next();
			String key = next.getKey();
			//if (!pruneKeywords) {
			//	if (getPos(key)==KEYWORD_POS)
			//		continue; //do not prune keywords
			//}
			int value = next.getValue()[0];
			if (value<=nodeFreqThreshold) {
				iterLabelsFreq.remove();
				matrixRowKeys.remove(key);
				matrixColumnKeys.remove(key);
			}
		}
			
		//prune by arcs weights
		Iterator<Cell<String, String, int[]>> iterMatrix = finalMatrix.cellSet().iterator();		
		while(iterMatrix.hasNext()) {
			Cell<String, String, int[]> next = iterMatrix.next();
			//if (!pruneKeywords) {
			//	if (getPos(next.getRowKey())==KEYWORD_POS || getPos(next.getColumnKey())==KEYWORD_POS)
			//		continue; //do not prune keywords if linked to other node with weights lower than threshold
			//}
			if (  next.getValue()[0]<=arcWeightsThreshold ) {
				iterMatrix.remove();
			}
		}
	}
	
	private static void exportMatrixToFiles(File outputFile) throws Exception {
		
		out.println("\n---- BUILDING GEXF FILE: " + outputFile);
		
		int number_of_nodes = 0;
		int number_of_edges = 0;				
		
		File matrixFile = FileUtil.replaceExtension(false, outputFile, "_edgeList.tsv");
		PrintWriter pwEdgeList = new PrintWriter(matrixFile);
		
		//graphML
		WeightedGraph<String, DefaultWeightedEdge> graphml = 
				new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		File graphmlFile = FileUtil.changeExtension(outputFile, "graphml");
		PrintWriter graphmlPw = new PrintWriter(graphmlFile);
		GraphmlExporter graphmlExp= new GraphmlExporter(graphmlPw);
		
		//gexf
		Gexf gexf = new GexfImpl();
		Calendar date = Calendar.getInstance();
		
		gexf.getMetadata()
			.setLastModified(date.getTime())
			.setCreator("Federico Sangati")
			.setDescription("MADRE project network visualization");
		gexf.setVisualization(true);

		Graph gexfGraph = gexf.getGraph();
		gexfGraph.setDefaultEdgeType(EdgeType.UNDIRECTED).setMode(Mode.STATIC);
		
		
		AttributeList attrListNodes = new AttributeListImpl(AttributeClass.NODE);
		gexfGraph.getAttributeLists().add(attrListNodes);
		Attribute attPosType = attrListNodes.createAttribute("0", AttributeType.STRING, "PoS_Type");
	 	
		
		int maxSize = (int)nodeFreqStats.getMax();
		int maxDegree = Utility.getMaxValueInt(labels_degree);
		int maxWeight = (int)arcWeightsStats.getMax();
		
		
		HashMap<String,Node> nodeTable = new HashMap<String,Node>();
		
		for(Cell<String, String, int[]> cs : finalMatrix.cellSet()) {
			int weight = cs.getValue()[0];
			
			String[] labels_RC = new String[]{cs.getRowKey(), cs.getColumnKey()};
			char[] pos_RC = new char[2];
			Node[] nodes_RC = new Node[2];
			for(int i=0; i<2; i++) {
				String label = labels_RC[i];
				Node n = nodeTable.get(label);
				char posType = getPos(label);
				pos_RC[i] = posType;
				if (n == null) {
					int freq = labels_freq.get(label)[0];		
					int degree = labels_degree.get(label)[0];
					String lemma = label.substring(0, label.lastIndexOf('_'));
					number_of_nodes++;
					graphml.addVertex(label);
					graphmlExp.printNode(label, lemma, posType, degree);
					n = gexfGraph.createNode(label);
					n.setLabel(lemma);
					n.setSize(scaleSize(
						nodeSizeBasedOnDegree ? degree : freq, 
						nodeSizeBasedOnDegree ? maxDegree : maxSize)
					);										
					n.getShapeEntity().setNodeShape(NodeShape.DIAMOND);										
					n.setColor(getColorPos(posType));
					n.getAttributeValues().addValue(attPosType, Character.toString(posType));										
					nodeTable.put(label, n);
				}
				nodes_RC[i] = n;
			}
			
			Edge gexfEdge = nodes_RC[0].connectTo(nodes_RC[1]);
			gexfEdge.setColor(getArcColor(pos_RC[0],pos_RC[1]));
			gexfEdge.setWeight(scaleEdge(weight, maxWeight));
			
			DefaultWeightedEdge graphmlEdge = graphml.addEdge(labels_RC[0], labels_RC[1]);
			graphml.setEdgeWeight(graphmlEdge, weight); 
			graphmlExp.printEdge(labels_RC[0] , labels_RC[1], weight);
			
			pwEdgeList.println(labels_RC[0] + '\t' + labels_RC[1] + '\t' + weight);
			
			
			//e.setThickness(scaleEdge(weight, maxWeight));			
			number_of_edges++;
		}
		
		pwEdgeList.close();
		
		File gexfFile = FileUtil.changeExtension(outputFile, "gexf");
		StaxGraphWriter graphWriter = new StaxGraphWriter();		
		Writer gexfWriter =  new FileWriter(gexfFile, false);
		graphWriter.writeToStream(gexf, gexfWriter, "UTF-8");
		
		 
		graphmlExp.close();
		//FileWriter graphmlWriter = new FileWriter(graphmlFile);
		//GraphMLExporter<String, DefaultWeightedEdge> exporter = 
		//		new GraphMLExporter<String, DefaultWeightedEdge>(
		//				new IntegerNameProvider<String>(), new StringNameProvider<String>(), 
		//				new IntegerEdgeNameProvider<DefaultWeightedEdge>(), new StringEdgeNameProvider<DefaultWeightedEdge>());
		//exporter.export(graphmlWriter, graphml);
		
		out.println("Numer of nodes: " + number_of_nodes);
		out.println("Numer of edges: " + number_of_edges);
		
	}
	

	
	private static char getPos(String label) {
		return label.charAt(label.lastIndexOf('_')+1);
	}

	private static Color getColorPos(char posType) {
		return colorsPos[Arrays.binarySearch(posGroupLabels,posType)];
	}

	private static Color getArcColor(char p1, char p2) {
		if (p1==KEYWORD_POS || p2==KEYWORD_POS)
			return getColorPos(KEYWORD_POS);
		if (p1==VERB_POS || p2==VERB_POS)
			return getColorPos(VERB_POS);
		if (p1==ADJECTIVE_POS || p2==ADJECTIVE_POS)
			return getColorPos(ADJECTIVE_POS);
		return getColorPos(NOUN_POS);
	}

	private static float scaleEdge(int freq, int max) {
		return (int) Math.ceil((double)freq/max*10);
	}
	
	private static float scaleSize(int freq, int max) {
		return 10 + (int) Math.ceil((double)freq/max*100);
	}

	final static String[] pruneMode = new String[]{
		"no_cutoff", //0
		"mean", //0
		"mean + 1 SD", //1
	};
	
	static void printBasicStatistics() {
		out.println("\n----- BASIC STATS -----");
		out.println("Number of total sentences: " + totalSentences);
		out.println("Number of selected sentences: " + selectedSentences);		
		out.println("Number of tokens: " + Utility.totalSumValues(lemma_freq));
		if (keywordsLength!=null)
			out.println("Number of found keywords types in text: " + foundKeyWords.size());			
		out.println("Lemma types: " + lemma_freq.size());
		out.println("Lemma sample: " + Arrays.toString(Arrays.copyOfRange(lemma_freq.keySet().toArray(),0,10)) + "...");
		out.println("PoS types: " + pos_freq.size());		
		out.println("PoS: " + Utility.tableIntToString(pos_freq));
		//out.println("PoS groups: " + Arrays.toString(posGroupLabels));
		out.println("Labels types: " + labels_freq.size());
		out.println("Labels sample: " + Arrays.toString(Arrays.copyOfRange(labels_freq.keySet().toArray(),0,10)) + "...");
	}
	
	static void printFreqLemmas(File outputFile, int minFreq) throws FileNotFoundException {
		//System.out.println("\n---- PRINTING FREQ LEMMAS ----- ");
						
		for(Character p : pos_lemma_freq.keySet()) {
			File f = FileUtil.replaceExtension(false, outputFile, "_"+p+".tsv");
			HashMap<String, int[]> subTable = pos_lemma_freq.get(p);			
			printFreqLemmas(subTable, f, minFreq);
		}
		File f = FileUtil.replaceExtension(false, outputFile, "_ALL.tsv");
		printFreqLemmas(labels_freq, f, minFreq);
		
	}
	
	
	private static void printFreqLemmas(HashMap<String, int[]> subTable, 
			File outputFile, int minFreq) throws FileNotFoundException {
		double cum = 0;		
		HashMap<String, Integer> intTable = Utility.convertHashMapIntArrayInteger(subTable);
		TreeMap<Integer, HashSet<String>> inverted = Utility.reverseAndSortTable(intTable);
		int totFreq = Utility.totalSumValues(subTable);
		PrintWriter pw = new PrintWriter(outputFile); 			
		Iterator<Entry<Integer, HashSet<String>>> iter = inverted.descendingMap().entrySet().iterator();
		while(iter.hasNext()) {
			Entry<Integer, HashSet<String>> next = iter.next();
			HashSet<String> lemmaSet = next.getValue();
			double freq = next.getKey();
			if (freq<minFreq)
				break;
			for(String l : lemmaSet) {
				double ratio = freq/totFreq;
				cum += ratio;
				pw.println(l + '\t' + freq + '\t' + ratio + '\t' + cum);
			}
		}
		pw.close();		
	}

	static void removeLabelsNotInMatrix() {

		out.println("\n----- REMOVING LABELS NOT IN MATRIX -----");
		out.println("Original number of labels: " + labels_freq.size());
		
		HashSet<String> labelSetInMatrix = new HashSet<String>(finalMatrix.columnKeySet()); 
		labelSetInMatrix.addAll(finalMatrix.rowKeySet());
		labels_freq.keySet().retainAll(labelSetInMatrix);
		out.println("New number of labels: " + labels_freq.size());
				
	}
	
	static void printNodeFreqStatistics(boolean advancedStats) {
		out.println();
		out.println("----- FREQ STATS -----");
		out.println(getStatsSummary(nodeFreqStats, advancedStats));
	}
	
	static void printArcWeightStatistics(boolean advancedStats) {
		out.println();
		out.println("----- ARCS STATS -----");
		out.println(getStatsSummary(arcWeightsStats, advancedStats));
	}
	
	static void printNodeDegreeStatistics(boolean advancedStats) {
		out.println();
		out.println("----- DEGREE STATS -----");
		out.println(getStatsSummary(nodeDegreeStats, advancedStats));
		
		
		HashMap<String, Double> table = Utility.convertHashMapIntArrayDouble(labels_degree_norm);
		TreeMap<Double, String> orderedTable = Utility.invertHashMapInTreeMap(table);
		double threashold = nodeDegreeStats.getMean() + nodeDegreeStats.getStandardDeviation();
		
		out.println("\nNodes with degree >= mean + 1SD [" + threashold + "]:");
		
		SortedMap<Double, String> tailMap = orderedTable.tailMap(threashold);
		out.println(tailMap);
	}
	
	private static String getStatsSummary(DescriptiveStatistics stats, boolean advanced) {
				
		StringBuilder sb = new StringBuilder();
		double[] values = stats.getValues();
		double mean = stats.getMean();
		double standardDeviation = stats.getStandardDeviation();
	
		double[] meanPlus_x_SD = new double[maxSD+1];
		for(int x = 0; x<=maxSD; x++) {
			meanPlus_x_SD[x] = mean + standardDeviation*x;
		}
		
		int[] elements_above_meanPlus_x_SD = new int[maxSD+1];
		
		for(double v : values) {
			for(int x = 0; x<=maxSD; x++) {
				if (v>meanPlus_x_SD[x]) {
					elements_above_meanPlus_x_SD[x]++;
				}
				else
					continue;
			}
		}
		
		sb.append("n: ").append(stats.getN()).append('\n');
		sb.append("min: ").append(stats.getMin()).append('\n');
		sb.append("max: ").append(stats.getMax()).append('\n');
		sb.append("mean: ").append(mean).append('\n');
		sb.append("std dev: ").append(standardDeviation).append('\n');
		
		if (advanced) {
			sb.append("n > mean: ").append(elements_above_meanPlus_x_SD[0]).append('\n');
			for(int x = 1; x<=maxSD; x++) {
				sb.append("n > mean + " + x + "SD: ").append(elements_above_meanPlus_x_SD[x]).append('\n');
			}			
		}
		sb.append("skewness: ").append(stats.getSkewness()).append('\n');
		sb.append("kurtosis: ").append(stats.getKurtosis());
		return sb.toString();
	}

	private static void printFinalStatistics(File logFile) throws FileNotFoundException {		
								
		printBasicStatistics();
		printNodeFreqStatistics(false);
		printArcWeightStatistics(false);
		
		computeDegreeStats(true);						
		printNodeDegreeStatistics(false);
		
		
	}

	private static void computeDegreeStats(boolean normalize) {
		Iterator<Cell<String, String, int[]>> iterMatrix = finalMatrix.cellSet().iterator();		
		while(iterMatrix.hasNext()) {
			Cell<String, String, int[]> cell = iterMatrix.next();
			String c = cell.getColumnKey();			
			String r = cell.getRowKey();
			//int value = cell.getValue()[0];
			Utility.increaseInHashMap(labels_degree_norm, c, 1);
			Utility.increaseInHashMap(labels_degree_norm, r, 1);
			Utility.increaseInHashMap(labels_degree,c);
			Utility.increaseInHashMap(labels_degree,r);
		}
		
		nodeDegreeStats = getStatsDouble(labels_degree_norm.values());
		
		if (normalize) {		
			double max = nodeDegreeStats.getMax();
			for(double[] d : labels_degree_norm.values()) {
				d[0] = d[0]/max;
			}
			
			nodeDegreeStats = getStatsDouble(labels_degree_norm.values());
		}
	}
	


	static final DecimalFormat dm = new DecimalFormat("    0.00");
	
	private static void computeBetweennessCentrality(boolean weighted, int printTopN) {
		String w = weighted ? "(WEIGHTED)" : "(UNWEIGHTED)";
		out.println("\n----- COMPUTING BETWEENNESS_CENTRALITY " + w + "-----");
		
		SingleGraph graph = new SingleGraph("Betweenness Test");
		BetweennessCentrality bcb = new BetweennessCentrality();
		if (weighted)
			bcb.setWeightAttributeName("weight");
		else 
			bcb.setUnweighted();
		
		for(String l : labels_freq.keySet()) {
			graph.addNode(l);
		}
		
		for(Cell<String, String, int[]> cs : finalMatrix.cellSet()) {
			String lR = cs.getRowKey();
			String lC = cs.getColumnKey();
			//org.graphstream.graph.Edge e = 
			graph.addEdge(lR+ "_" + lC, lR, lC, false);
			
			if (weighted) {
				double weight = cs.getValue()[0]; //+(int)(Math.random()*500); //cs.getValue()[0];// 800; //((double)cs.getValue()[0])/100; //(int)(Math.random()*1);
				bcb.setWeight(graph.getNode(lR), graph.getNode(lC), weight); 
				////graph.setAttribute("weight", cs.getValue()[0]);
			}
		}
		
		bcb.registerProgressIndicator(new Progress() {			
			int value=0;			
			@Override
			public void progress(float percentage) {
				//out.println(percentage);
				int f = (int)(percentage*100); 
				if (f>value) {
					while(f>value) {
						value++;
						char c = value%10==0 ? '+' : '.';
						System.out.print(c);
					}					
				}				
			}
		});	   
		
		System.out.print('|');
        bcb.init(graph);
        bcb.compute();
        System.out.println('|');
        
        HashMultimap<Double,String> scoreTable = HashMultimap.create();
        
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for(String l : labels_freq.keySet()) {
        	org.graphstream.graph.Node n = graph.getNode(l);
        	Double cb = n.getAttribute("Cb");
        	scoreTable.put(cb, l);
        	stats.addValue(cb);
        }
        
        getStatsSummary(stats, false);
        //out.println("\nMEAN: " + dm.format(stats.getMean()));
        
        out.println("\nTOP candidates:");
       
        TreeSet<Double> sortedSet = new TreeSet<Double>(scoreTable.keySet());
        Iterator<Double> iter = sortedSet.descendingIterator();
        boolean all = scoreTable.size() < 2*printTopN;
		int count=0;
		while(iter.hasNext()) {
			Double cb = iter.next();
			Set<String> labels = scoreTable.get(cb);
			for(String l : labels) {
				out.println(dm.format(cb) + "\t" + l);				
			}
			if (!all && ++count==printTopN)
				break;
		}

		out.println("    ...");

		
        

        //out.println("\nDebug:");
        //double donnaScore = graph.getNode("donna_S").getAttribute("Cb");
        //out.println("Node Centrality (donna_S):   " + donnaScore);
	}
	

	public static void cleanCsv(File dir) throws FileNotFoundException {
		for(File f : dir.listFiles()) {
			if (!f.getName().endsWith(".csv"))
				continue;
			out.println("Cleaning " + f);
			Scanner scan = null;
			try {
				scan = new Scanner(f);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			File o = new File(f.getAbsolutePath() + "~");
			PrintWriter pw = new PrintWriter(o);
			while(scan.hasNextLine()) {
				String line = scan.nextLine();
				pw.println(line.replaceAll("\\\\\"", "\"\""));
			}
			pw.close();
			o.renameTo(f);
			scan.close();
		}
	}
	
	public static void TestRemoveOverlapping(String[] args) throws IOException {
		int size = 10;
		boolean[][] matrix = new boolean[size][size];
		for(int i=0; i<size*size/2; i++) {
			int x = (int)(Math.random()*size);
			int y = (int)(Math.random()*size);
			if (x>y)
				matrix[y][x] = true;
		}
		Utility.printChart(matrix);
		removeOverlapping(matrix);
		System.out.println();
		Utility.printChart(matrix);
	}
	

	public static void main(String[] args) throws Exception {

		//
		// boolean pruneMatrixMode = 0;

		// 0: mean
		// 1: mean+1SD
		// 2: mean+2SD
		// 3: mean+3SD
		// ...
		useKeywords = false;
		nodeSizeBasedOnDegree = true;
		int nodeSDcutOff = 2;
		int arcSDcutOff = 2;
		// pruneKeywords = false;

		String path = "/Volumes/HardDisk/Scratch/Projects/MADRE/";
		String corporaPath = path + "Corpora_Fixed/";
		String outputPath = path + "FinalNetworks/";
		File keywordFile = new File(path + "patterns.txt");

		// File inputFile = new File(path +
		// "Corpora/trentino_stampa_2011/trentino_stampa_2011.csv");

		for (File inputFile : new File(corporaPath).listFiles()) {
			if (!inputFile.getName().endsWith(".csv"))
				continue;
			
			//inputFile = new File(corporaPath + "trentino_stampa_2013.csv");

			System.out.println("--------MADRE_TextPro_Analysis--------");
			System.out.println("MODE: " + nodeSDcutOff + " " + arcSDcutOff);
			System.out.println("Input File: " + inputFile);

			init();

			String dirName = FileUtil.getFileNameWithoutExtensions(inputFile);
			File outputDir = new File(outputPath + dirName);
			outputDir.mkdir();
			
			File logFile = new File(outputDir.getAbsoluteFile() + "/" + dirName +
					"_mode_" + nodeSDcutOff + "_" + arcSDcutOff + ".txt");
			
			//File logFile = FileUtil.replaceExtension(false, inputFile,
			//		(useKeywords ? "_KWon_" : "_KWoff_") + "mode_"
			//				+ nodeSDcutOff + "_" + arcSDcutOff + ".txt");

			out = new PrintStream(logFile);
			// out = System.out;

			if (useKeywords)
				readKeywordFile(keywordFile);

			// File flatFile = FileUtil.changeExtension(inputFile, ".flat");
			// convertFileToFlat(inputFile, flatFile);

			File selectedFlatLemmaFile = new File(outputDir.getAbsoluteFile() + "/" + dirName + "_flat_lemma.txt");
			pw_selectedFlatLemma = new PrintWriter(selectedFlatLemmaFile);

			processCsvFile(inputFile);

			pw_selectedFlatLemma.close();

			printBasicStatistics();

			// printing lemmas freq distributionper categories
			//File freqFile = FileUtil.replaceExtension(false, inputFile,
			//		(useKeywords ? "_KWon_" : "_KWoff_") + "freq" + ".txt");
			//printFreqLemmas(freqFile, 4);

			removeLabelsNotInMatrix();
			computeStatistics();
			printNodeFreqStatistics(true);
			printArcWeightStatistics(true);

			pruneMatrix(nodeSDcutOff, -1);
			removeLabelsNotInMatrix();
			computeStatistics();
			printNodeFreqStatistics(false);
			printArcWeightStatistics(true);

			pruneMatrix(-1, arcSDcutOff);
			removeLabelsNotInMatrix();
			computeStatistics();
			printNodeFreqStatistics(false);
			printArcWeightStatistics(false);

			computeBetweennessCentrality(false, 10);
			computeBetweennessCentrality(true, 10);

			// printFinalStatistics(logFile);

			computeDegreeStats(true);
			printNodeDegreeStatistics(false);

			exportMatrixToFiles(logFile);

			out.close();			
			
			System.out.println("nodes: " + nodeFreqStats.getN());
			System.out.println("edges: " + arcWeightsStats.getN());
		}

	}




	
}

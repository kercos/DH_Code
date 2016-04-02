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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.graphstream.algorithm.BetweennessCentrality;
import org.graphstream.algorithm.BetweennessCentrality.Progress;
import org.graphstream.graph.implementations.SingleGraph;

import util.Connl_Stanford;
import util.FileUtil;
import util.Utility;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Table.Cell;


public class CostLetters_Network_Analysis {
	
	final static String[] selectedPoS = new String[]{
		"JJ",	//			adjective									nice, easy
		"JJR",	//			adjective, comparative						nicer, easier
		"JJS",	//			adjective, superlative						nicest, easiest
		"NN",	//			noun, singular or mass						tiger, chair, laughter
		"NNS",	//			noun, plural								tigers, chairs, insects
		"NNP",	//			noun, proper singular						Germany, God, Alice
		"NNPS",	//			noun, proper plural							we met two Christmases ago
		"VB",	//			verb, base form								think
		"VBZ",	//			verb, 3rd person singular present			she thinks
		"VBP",	//			verb, non-3rd person singular present		I think
		"VBD",	//			verb, past tense							they thought
		"VBN",	//			verb, past participle						a sunken ship
		"VBG"	//			verb, gerund or present participle			thinking is fun
	};
	
	static final char ADJECTIVE_POS = 'J';	
	static final char NOUN_POS = 'N';
	static final char VERB_POS = 'V';
	
	static final char[] posGroupLabels = new char[]{
		ADJECTIVE_POS, //'J'
		NOUN_POS, //'N',
		VERB_POS //'V'
	};
	
	static final Color[] colorsPos = new ColorImpl[]{
		new ColorImpl(0,255,0), 	// green adj (J)
		new ColorImpl(0,0,255), 	// blue nouns (N)
		new ColorImpl(255,0,0), 	// red verbs (V)
	};

	
	final static String[] displayedDepRel = new String[]{
		"dobj",
		"nsubj",
		"nsubjpass",		
	};

	static final Color[] colorsDep = new ColorImpl[]{
		new ColorImpl(0,0,255), 	// blue obj
		new ColorImpl(255,0,0), 	// red sbj
		new ColorImpl(255,0,0), 	// red sbjpass			
	};

	
	static final Color blackColor = new ColorImpl(0,0,0);
	
	static final HashSet<String> excludedLemmas = new HashSet<String>(
			Arrays.asList(new String[]{"be", "have", "unknown", "do", "shall", "may", "will", "bee", "mee", "can"}));
	
	static boolean BLACK_EDGES = false;
	
	final static int maxSD = 5;
	
	static int ROOT_INDEX = 0;
	static int HEAD_BASE_INDEX = 1;
	
	static HashMap<String, int[]> lemma_freq = new HashMap<String, int[]>();
	static HashMap<String, int[]> pos_freq = new HashMap<String, int[]>();
	static HashMap<String, int[]> depRel_freq = new HashMap<String, int[]>();
	static HashMap<LemmaPos, int[]> token_freq = new HashMap<LemmaPos, int[]>();
	static HashBasedTable<LemmaPosDepRel, LemmaPosDepRel, int[]> finalMatrix = HashBasedTable.create();
	static int numberOfSentences;
	static DescriptiveStatistics nodeFreqStats, arcWeightsStats;
	
	static boolean pruningBasedOnMean = true;
	
	private static void init() {
		lemma_freq = new HashMap<String, int[]>();
		pos_freq = new HashMap<String, int[]>();
		depRel_freq = new HashMap<String, int[]>();
		token_freq = new HashMap<LemmaPos, int[]>();
		finalMatrix = HashBasedTable.create();
		numberOfSentences = 0;
	}
	
	// coMatrix[w1][w2] exists if w1<w2 (in alphabetical order)
	// it contains the number of time w1 is in proximity of w2 (before or after a window size of words)
	
	static boolean linkByDependency = false;
	
	static {
		Arrays.sort(selectedPoS);
		Arrays.sort(displayedDepRel);
	}

	private static Character resolvePosGroup(String pos) {
		if (Arrays.binarySearch(selectedPoS, pos)>=0) {
			return pos.charAt(0);
		}
		return null;
	}

	
	// coMatrix[w1][w2] exists if w1<w2 (in alphabetical order)
	// it contains the number of time w1 is in proximity of w2 (before or after a window size of words)

	static void processFileMorphAdorner(File fileConll) throws FileNotFoundException, IOException {
		System.out.println("Processing file: " + fileConll);
		
		Scanner scan = new Scanner(fileConll);		
		
		numberOfSentences++;		
		
		HashSet<Character> posSet = new HashSet<Character>(
				Arrays.asList(new Character[]{'N','V','J'})
				); 
		
		while(true) {
			String[][] sntFields = Connl_Stanford.getNextGenericConnlLinesSentence(scan);
			if (sntFields==null)
				break;
			numberOfSentences++;
			LinkedList<ConllToken> token_sentence = new LinkedList<ConllToken>();
			for(String[] fields: sntFields) {
				String form = fields[1];
				String lemma = fields[2];
				String fullPos = fields[3];
				Character pos = Character.toUpperCase(fullPos.charAt(0));
				if (!posSet.contains(pos))
					pos = null;
				ConllToken token = new ConllToken(form, lemma, pos, "ner", -1, "deprel");
				token_sentence.add(token);				
			}
			if (linkByDependency) {
				processSentenceByDepDistance(token_sentence);
				//processSentenceByDependencies(token_sentence);
			}	
			else
				processSentence(token_sentence);
		}
			
		scan.close();
	}	
	
	static void processFileSentence(File fileConll) throws FileNotFoundException, IOException {
		System.out.println("Processing file: " + fileConll);
		
		Scanner scan = new Scanner(fileConll);		
		
		while(true) {
			Connl_Stanford snt = Connl_Stanford.getNextConnlLinesSentence(scan);		
			if (snt==null)
				break;
			numberOfSentences++;
			LinkedList<ConllToken> token_sentence = new LinkedList<ConllToken>();
			for(int i=0; i<snt.length; i++) {
				Character pos = resolvePosGroup(snt.postags[i]);
				ConllToken token = new ConllToken(
						snt.forms[i], snt.lemmas[i], pos, snt.ner[i], snt.heads[i], snt.deprels[i]);
				token_sentence.add(token);				
			}
			if (linkByDependency)
				processSentenceByDependencies(token_sentence);
			else
				processSentence(token_sentence);
			
		}

	}	

	private static void processSentence(LinkedList<ConllToken> token_sentence) {
		ListIterator<ConllToken> iterFirst = token_sentence.listIterator();
		while(iterFirst.hasNext()) {
			ConllToken next = iterFirst.next();
			if (!excludedLemmas.contains(next.lemma) && next.pos!=null) {
				Utility.increaseInHashMap(lemma_freq, next.lemma);
				Utility.increaseInHashMap(pos_freq, next.pos.toString());
				//String lemmaPosGroup = next.lemmaPos;
				Utility.increaseInHashMap(token_freq, next.getLemmaPos());			
			}
			else
				iterFirst.remove();
		}
				
		iterFirst = token_sentence.listIterator();
		ListIterator<ConllToken> iterSecond = null;
		LemmaPosDepRel first = null, second = null;
		while(iterFirst.hasNext()) {			
			first = iterFirst.next().getLemmaPosDepRel();
			iterSecond = token_sentence.listIterator(iterFirst.nextIndex());
			while(iterSecond.hasNext()) {
				second = iterSecond.next().getLemmaPosDepRel();
				int cmp = first.toString().compareTo(second.toString());
				if (cmp==0)
					continue;
				else if (cmp>0)
					increaseInMatrix(first, second);
				else 
					increaseInMatrix(first, second);
			}
		}	
	}

	private static void processSentenceByDependencies(LinkedList<ConllToken> token_sentence) {
		
		ListIterator<ConllToken> iterFirst = token_sentence.listIterator();
		while(iterFirst.hasNext()) {
			ConllToken currentToken = iterFirst.next(); //(TextTokenDepRel) 
			int head = currentToken.head;
			if (head<=ROOT_INDEX) //0 or -1
				continue; // exlude roots
			
			LemmaPosDepRel currentTokenLemmaPos = currentToken.getLemmaPosDepRel(); //getLemmaPosDepRel()
			LemmaPosDepRel headTokenLemmaPos = token_sentence.get(head - HEAD_BASE_INDEX).getLemmaPosDepRel(); //getLemmaPosDepRel()
			
			if (currentTokenLemmaPos.pos!=null && !excludedLemmas.contains(currentTokenLemmaPos.lemma)) {
				Utility.increaseInHashMap(lemma_freq, currentTokenLemmaPos.lemma);
				Utility.increaseInHashMap(pos_freq, currentTokenLemmaPos.pos.toString());				
				Utility.increaseInHashMap(depRel_freq, currentTokenLemmaPos.depRel);
				
				//String currentLabel = currentToken.lemmaPos;
				Utility.increaseInHashMap(token_freq, currentTokenLemmaPos);
				
				if (headTokenLemmaPos.pos!=null && !excludedLemmas.contains(headTokenLemmaPos.lemma)) {
					//String headLabel = headToken.lemmaPos;
					increaseInMatrix(currentTokenLemmaPos, headTokenLemmaPos);
				}	
			}
					  
		}						
	}
	
	static HashSet<Character> verbNounposSet = new HashSet<Character>(
			Arrays.asList(new Character[]{'N','V'})
			);
	
	static HashSet<String> relationsSet = new HashSet<String>(
			Arrays.asList(displayedDepRel));

	
	static void processFileTriplets(File fileTriplets) throws FileNotFoundException, IOException {
		System.out.println("Processing file: " + fileTriplets);		
		Scanner scan = new Scanner(fileTriplets);
		int totTriplets = 0;
		int acceptedTriplets = 0;		
		while(scan.hasNextLine()) {
			String nextLine = scan.nextLine();
			if (nextLine.isEmpty())
				continue;
			totTriplets++;
			String[] nextLineSplit = nextLine.split("\t");
			String[] firstWordPos = nextLineSplit[0].replaceAll("\\s+", "").split("\\|");
			String[] secondWordPos = nextLineSplit[1].replaceAll("\\s+", "").split("\\|");
			String rel = nextLineSplit[2].replaceAll("\\s+", "");
			int freq = Integer.parseInt(nextLineSplit[3]);			
			if (!verbNounposSet.contains(firstWordPos[1].charAt(0)) ||
				!verbNounposSet.contains(secondWordPos[1].charAt(0))	||				
				!relationsSet.contains(rel))
				continue;
					
			String[] verbWordPos = firstWordPos[1].charAt(0)=='V' ? firstWordPos : secondWordPos;
			String[] notVerbWordPos = firstWordPos[1].charAt(0)=='V' ? secondWordPos : firstWordPos;
			LemmaPosDepRel depTokenLemmaPos = new LemmaPosDepRel(notVerbWordPos[0], notVerbWordPos[1].charAt(0), rel);
			LemmaPosDepRel verbTokenLemmaPos = new LemmaPosDepRel(verbWordPos[0], verbWordPos[1].charAt(0), "deprel");
			if (excludedLemmas.contains(depTokenLemmaPos.lemma) || excludedLemmas.contains(verbTokenLemmaPos.lemma))
				continue;
			
			Utility.increaseInHashMap(token_freq, verbTokenLemmaPos.toLemmaPos());
			Utility.increaseInHashMap(lemma_freq, verbTokenLemmaPos.lemma);
			Utility.increaseInHashMap(pos_freq, verbTokenLemmaPos.pos.toString());								

			Utility.increaseInHashMap(token_freq, depTokenLemmaPos.toLemmaPos());
			Utility.increaseInHashMap(lemma_freq, depTokenLemmaPos.lemma);
			Utility.increaseInHashMap(pos_freq, depTokenLemmaPos.pos.toString());
			Utility.increaseInHashMap(depRel_freq, depTokenLemmaPos.depRel);						
			increaseInMatrix(depTokenLemmaPos, verbTokenLemmaPos);						

			acceptedTriplets++;
		}
		
		System.out.println("Tot triplets: " + totTriplets);
		System.out.println("Accepted triplets: " + acceptedTriplets);
		
	}
	
	static final String[][] depDistanceRel = {
		new String[]{"N","V","-4","0","nsubj"},
		new String[]{"N","V","0","4","dobj"},
	};
	
	private static void processSentenceByDepDistance(LinkedList<ConllToken> token_sentence) {
		
		ConllToken[] conllTokens = token_sentence.toArray(new ConllToken[token_sentence.size()]);
		boolean[] usedTokens = new boolean[token_sentence.size()];
		
		for(int i=0; i<conllTokens.length; i++) {
			ConllToken verbToken = conllTokens[i];
			if (verbToken.pos!=null && verbToken.pos.equals('V')) {
				if (excludedLemmas.contains(verbToken.lemma))
					continue;				
				ConllToken sbjToken = null;
				ConllToken objToken = null;
				for(int j=i-1; j>=Math.max(0, i-4); j--) {
					ConllToken tokenJ = conllTokens[j];
					if (tokenJ.pos!=null && tokenJ.pos.equals('N')) {
						if (usedTokens[j] || excludedLemmas.contains(tokenJ.lemma))
							continue;
						sbjToken = tokenJ;
						sbjToken.depRel = "nsubj";
						usedTokens[j] = true;
						
					}
				}
				
				for(int j=i+1; j<=Math.min(conllTokens.length-1, i+4); j++) {
					ConllToken tokenJ = conllTokens[j];
					if (tokenJ.pos!=null && tokenJ.pos.equals('N')) {
						if (usedTokens[j] || excludedLemmas.contains(tokenJ.lemma))
							continue;
						objToken = tokenJ;
						objToken.depRel = "dobj";
						usedTokens[j] = true;						
					}
				}
				
				if (sbjToken!=null || objToken!=null) {
					Utility.increaseInHashMap(token_freq, verbToken.getLemmaPos());
					Utility.increaseInHashMap(lemma_freq, verbToken.lemma);
					Utility.increaseInHashMap(pos_freq, verbToken.pos.toString());								
					if (sbjToken!=null) {
						Utility.increaseInHashMap(token_freq, sbjToken.getLemmaPos());
						Utility.increaseInHashMap(lemma_freq, sbjToken.lemma);
						Utility.increaseInHashMap(pos_freq, sbjToken.pos.toString());
						Utility.increaseInHashMap(depRel_freq, sbjToken.depRel);						
						increaseInMatrix(sbjToken.getLemmaPosDepRel(), verbToken.getLemmaPosDepRel());						
					}
					if (objToken!=null) {
						Utility.increaseInHashMap(token_freq, objToken.getLemmaPos());
						Utility.increaseInHashMap(lemma_freq, objToken.lemma);
						Utility.increaseInHashMap(pos_freq, objToken.pos.toString());
						Utility.increaseInHashMap(depRel_freq, objToken.depRel);						
						increaseInMatrix(objToken.getLemmaPosDepRel(), verbToken.getLemmaPosDepRel());
					}
				}
				
			}				
		}
	}

	private static void increaseInMatrix(LemmaPosDepRel first, LemmaPosDepRel second) {
		int[] count = finalMatrix.get(first, second);
		if (count==null) {
			count = new int[]{0};
			finalMatrix.put(first, second, count);
		}
		count[0]++;		
	}

	static void removeLabelsNotInMatrix() {
	
		System.out.println("\n----- REMOVING LABELS NOT IN MATRIX -----");
		System.out.println("Original number of labels: " + token_freq.size());
		
		HashSet<LemmaPos> labelSetInMatrix = new HashSet<LemmaPos>(finalMatrix.columnKeySet()); 
		labelSetInMatrix.addAll(finalMatrix.rowKeySet());
		token_freq.keySet().retainAll(labelSetInMatrix);
		System.out.println("New number of labels: " + token_freq.size());
		
		nodeFreqStats = getStats(token_freq.values());
		arcWeightsStats = getStats(finalMatrix.values());
	}

	private static void pruneMatrix(int nodeCutOff, int arcCutOff) {
		System.out.println("\n" + "------ PRUNING MATRIX -------");
		double nodeFreqThreshold = 0;
		double arcWeightsThreshold = 0;
		
		if (nodeCutOff==-1) 
			System.out.println("Prune mode nodes: freq > 0 (no pruning)");
		else {
			if (pruningBasedOnMean) {
				nodeFreqThreshold = nodeFreqStats.getMean() + nodeFreqStats.getStandardDeviation()*nodeCutOff;
				System.out.println("Prune mode nodes: freq > mean" + "+ " + nodeCutOff + "SD [" + nodeFreqThreshold + "]");
			}
			else {
				nodeFreqThreshold = nodeCutOff;
				System.out.println("Prune mode nodes: freq > " + nodeCutOff);
			}
		}
		
		if (arcCutOff==-1)
			System.out.println("Prune mode arcs: weights > 0 (no pruning)");
		else {
			if (pruningBasedOnMean) {
				arcWeightsThreshold = arcWeightsStats.getMean() + arcWeightsStats.getStandardDeviation()*arcCutOff;
				System.out.println("Prune mode arcs:  weight > mean " + "+ " + arcCutOff + "SD [" + arcWeightsThreshold + "]");
			}
			else {
				arcWeightsThreshold = arcCutOff;
				System.out.println("Prune mode arcs:  weight > " + arcCutOff);
			}
		}
		 		
		Iterator<Entry<LemmaPos, int[]>> iterLabelsFreq = token_freq.entrySet().iterator();
		while(iterLabelsFreq.hasNext()) {
			Entry<LemmaPos, int[]> next = iterLabelsFreq.next();
			if (next.getValue()[0]<=nodeFreqThreshold)
				iterLabelsFreq.remove();
		}
		
		Iterator<Cell<LemmaPosDepRel, LemmaPosDepRel, int[]>> iterMatrix = finalMatrix.cellSet().iterator();		
		while(iterMatrix.hasNext()) {
			Cell<LemmaPosDepRel, LemmaPosDepRel, int[]> next = iterMatrix.next();
			LemmaPos columnKey = next.getColumnKey().toLemmaPos();
			LemmaPos rowKey = next.getRowKey().toLemmaPos();
			if (  next.getValue()[0]<=arcWeightsThreshold ||
				! token_freq.containsKey(columnKey) ||
				! token_freq.containsKey(rowKey) ) {
	
				iterMatrix.remove();
			}
		}
	}

	private static void buildGexfFile(File outputFile, File logFile) throws FileNotFoundException {
		
		System.out.println("\n---- BUILDING GEXF FILE: " + outputFile);
		
		int number_of_nodes = 0;
		int number_of_edges = 0;		

		
		Gexf gexf = new GexfImpl();
		Calendar date = Calendar.getInstance();
		
		gexf.getMetadata()
			.setLastModified(date.getTime())
			.setCreator("Federico Sangati")
			.setDescription("Repubblic of Letters project network visualization");
		gexf.setVisualization(true);

		Graph graph = gexf.getGraph();
				
		graph.setDefaultEdgeType(linkByDependency ? EdgeType.DIRECTED : EdgeType.UNDIRECTED);
		graph.setMode(Mode.STATIC);
						
		AttributeList attrListNodes = new AttributeListImpl(AttributeClass.NODE);
		graph.getAttributeLists().add(attrListNodes);
		Attribute attPosType = attrListNodes.createAttribute("0", AttributeType.STRING, "PoS_Type");
	 	
		
		int maxWeight = (int)arcWeightsStats.getMax();
		int maxSize = (int)nodeFreqStats.getMax();
		
		HashMap<LemmaPos,Node> nodeTable = new HashMap<LemmaPos,Node>();
		
		for(Cell<LemmaPosDepRel, LemmaPosDepRel, int[]> cs : finalMatrix.cellSet()) {
			int weight = cs.getValue()[0];
			
			LemmaPosDepRel[] tokens_RC = new LemmaPosDepRel[]{cs.getRowKey(), cs.getColumnKey()};
			char[] pos_RC = new char[2];
			Node[] nodes_RC = new Node[2];
			for(int i=0; i<2; i++) {
				LemmaPos token = tokens_RC[i].toLemmaPos();
				Node n = nodeTable.get(token);
				char posType = token.pos;
				pos_RC[i] = posType;
				if (n == null) {
					int freq = token_freq.get(token)[0];
					number_of_nodes++;
					n = graph.createNode(token.lemmaPos()); //token.toString()
					n.setLabel(token.lemma); //token.toString()
					n.setSize(scaleSize(freq,maxSize));										
					n.getShapeEntity().setNodeShape(NodeShape.DIAMOND);										
					n.setColor(getColorPos(posType));
					n.getAttributeValues().addValue(attPosType, Character.toString(posType));										
					nodeTable.put(token, n);
				}
				nodes_RC[i] = n;
			}
			
			if (!nodes_RC[0].hasEdgeTo(nodes_RC[1].getId())) {
				Edge e = nodes_RC[0].connectTo(nodes_RC[1]);
				e.setColor(BLACK_EDGES ? blackColor : getArcColor(pos_RC[0],pos_RC[1]));
				e.setWeight(scaleEdge(weight, maxWeight));
				if (linkByDependency) {
					String depRel = tokens_RC[0].depRel;
					//if (Arrays.binarySearch(displayedDepRel, depRel)<0)
					//	depRel = "";
					e.setLabel(depRel);		
					e.setColor(getColorDep(depRel));
				}
				number_of_edges++;
			}
		}
		
		StaxGraphWriter graphWriter = new StaxGraphWriter();		
		Writer out;
		try {
			out =  new FileWriter(outputFile, false);
			graphWriter.writeToStream(gexf, out, "UTF-8");
			System.out.println(outputFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		PrintWriter logWriter = new PrintWriter(logFile);
		
		System.out.println("Numer of nodes: " + number_of_nodes);		
		System.out.println("Numer of edges: " + number_of_edges);
		
		logWriter.println("Numer of nodes: " + number_of_nodes);
		logWriter.println("Numer of edges: " + number_of_edges);
		
		logWriter.close();
		
	}
	
	private static void buildMatrixfFile(File outputFile) throws FileNotFoundException {
		
		System.out.println("\n---- BUILDING MATRIX FILE: " + outputFile);
		
		PrintWriter pw = new PrintWriter(outputFile);
		
		for(Cell<LemmaPosDepRel, LemmaPosDepRel, int[]> cs : finalMatrix.cellSet()) {
			int weight = cs.getValue()[0];
			
			LemmaPos[] tokens_RC = new LemmaPos[]{cs.getRowKey().toLemmaPos(), cs.getColumnKey().toLemmaPos()};
			char[] pos_RC = new char[2];
			Node[] nodes_RC = new Node[2];
			for(int i=0; i<2; i++) {
				LemmaPos token = tokens_RC[i];
				char posType = token.pos;
				pos_RC[i] = posType;
			}
			
			String dep = "";
			if (linkByDependency)				
				dep = "\t" + tokens_RC[0].depRel;
			pw.println(tokens_RC[0] + "\t" + tokens_RC[1] + dep + "\t" + weight);
			
		}
		
		pw.close();
		
		
	}


	private static Color getColorPos(char posType) {
		return colorsPos[Arrays.binarySearch(posGroupLabels,posType)];
	}

	private static Color getColorDep(String depType) {
		return colorsDep[Arrays.binarySearch(displayedDepRel,depType)];
	}

	
	private static Color getArcColor(char p1, char p2) {
		/*
		if (linkByDependency) {
			return getColorPos(p1);
		}
		*/
		//else {
			if (p1==VERB_POS || p2==VERB_POS)
				return getColorPos(VERB_POS);
			if (p1==ADJECTIVE_POS || p2==ADJECTIVE_POS)
				return getColorPos(ADJECTIVE_POS);
			return getColorPos(NOUN_POS);
		//}		
	}

	private static float scaleEdge(int freq, int max) {
		return (int) Math.ceil((double)freq/max*10);
	}
	
	private static float scaleSize(int freq, int max) {
		return 10 + (int) Math.ceil((double)freq/max*100);
	}
	
	static void printBasicStatistics() {
		System.out.println("\n----- BASIC STATS -----");
		System.out.println("Number of sentences: " + numberOfSentences);
		System.out.println("Number of tokens: " + Utility.totalSumValues(lemma_freq));
		System.out.println("Lemma types: " + lemma_freq.size());
		System.out.println("Lemma sample: " + Arrays.toString(Arrays.copyOfRange(lemma_freq.keySet().toArray(),0,10)) + "...");
		System.out.println("PoS types: " + pos_freq.size());		
		System.out.println("PoS: " + Utility.tableIntToString(pos_freq));
		System.out.println("PoS groups: " + Arrays.toString(posGroupLabels));
		if (linkByDependency) {
			System.out.println("Dependency relations: " + Utility.tableIntToString(depRel_freq));
		}
		System.out.println("Labels types: " + token_freq.size());
		System.out.println("Labels sample: " + Arrays.toString(Arrays.copyOfRange(token_freq.keySet().toArray(),0,10)) + "...");
		
	}
	
	static void printMatrixStatistics(boolean advanced) {
		System.out.println();
		System.out.println("----- NETWORK STATS -----");
		System.out.println();
		System.out.println("Nodes stats:\n" + getSummary(nodeFreqStats, advanced));
		System.out.println();
		System.out.println("Arcs stats:\n" + getSummary(arcWeightsStats, advanced));
	}
	
	private static String getSummary(DescriptiveStatistics stats, boolean advanced) {
				
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

	private static DescriptiveStatistics getStats(Collection<int[]> values) {
		DescriptiveStatistics result = new DescriptiveStatistics();
		Iterator<int[]> iter = values.iterator();
		while(iter.hasNext()) {
			result.addValue(iter.next()[0]);
		}
		return result;
	}
	
	static final DecimalFormat dm = new DecimalFormat("    0.00");
	
	private static void computeBetweennessCentrality(boolean weighted, int printTopN) {
		String w = weighted ? "(WEIGHTED)" : "(UNWEIGHTED)";
		System.out.println("\n----- COMPUTING BETWEENNESS_CENTRALITY " + w + "-----");
		
		SingleGraph graph = new SingleGraph("Betweenness Test");
		BetweennessCentrality bcb = new BetweennessCentrality();
		if (weighted)
			bcb.setWeightAttributeName("weight");
		else 
			bcb.setUnweighted();
		
		for(LemmaPos l : token_freq.keySet()) {
			graph.addNode(l.toString());
		}
		
		for(Cell<LemmaPosDepRel, LemmaPosDepRel, int[]> cs : finalMatrix.cellSet()) {
			LemmaPos lR = cs.getRowKey();
			LemmaPos lC = cs.getColumnKey();
			String lR_label = lR.toString();
			String lC_label = lC.toString();
			//org.graphstream.graph.Edge e = 
			graph.addEdge(lR_label+ "_" + lC_label, lR_label, lC_label, false);
			
			if (weighted) {
				double weight = cs.getValue()[0]; //+(int)(Math.random()*500); //cs.getValue()[0];// 800; //((double)cs.getValue()[0])/100; //(int)(Math.random()*1);
				bcb.setWeight(graph.getNode(lR_label), graph.getNode(lC_label), weight); 
				////graph.setAttribute("weight", cs.getValue()[0]);
			}
		}
		
		bcb.registerProgressIndicator(new Progress() {			
			int value=0;			
			@Override
			public void progress(float percentage) {
				//System.out.println(percentage);
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
        for(LemmaPos l : token_freq.keySet()) {
        	String l_label = l.toString(); 
        	org.graphstream.graph.Node n = graph.getNode(l_label);
        	Double cb = n.getAttribute("Cb");
        	scoreTable.put(cb, l_label);
        	stats.addValue(cb);
        }
        
        System.out.println("\nMEAN: " + dm.format(stats.getMean()));
        
        System.out.println("\nTOP candidates:");
       
        TreeSet<Double> sortedSet = new TreeSet<Double>(scoreTable.keySet());
        Iterator<Double> iter = sortedSet.descendingIterator();
		int count=0;
		while(iter.hasNext()) {
			Double cb = iter.next();
			Set<String> labels = scoreTable.get(cb);
			for(String l : labels) {
				System.out.println(dm.format(cb) + "\t" + l);
				if (++count==printTopN) {
					if (iter.hasNext())
						System.out.println("    ...");
					while(iter.hasNext()) {
						cb = iter.next();
					}
					System.out.println(dm.format(cb) + "\t" + scoreTable.get(cb).iterator().next());
				}
			}
		}
        

        //System.out.println("\nDebug:");
        //double donnaScore = graph.getNode("donna_S").getAttribute("Cb");
        //System.out.println("Node Centrality (donna_S):   " + donnaScore);
	}
	
	
	
	public static void mainRepLettersStanford(String[] args) throws IOException {
		//
		//boolean pruneMatrixMode = 0;

		
		//0: mean
		//1: mean+1SD
		//2: mean+2SD
		//3: mean+3SD
		//...		
		int nodeSDcutOff = 0;  
		int arcSDcutOff = 2;
		linkByDependency = false;

		File fileCoNLL = null;
		if (args!=null && args.length>0) {
			fileCoNLL = new File(args[0]);
		}
		else {		
			System.err.println("Args needed");
			return;
		}
		
		processFileSentence(fileCoNLL);

		printBasicStatistics();
		
		removeLabelsNotInMatrix();		
		printMatrixStatistics(true);
		
		pruneMatrix(nodeSDcutOff, -1);		
		removeLabelsNotInMatrix();
		printMatrixStatistics(true);
		
		pruneMatrix(-1, arcSDcutOff);
		removeLabelsNotInMatrix();
		printMatrixStatistics(false);
		
		//computeBetweennessCentrality(false, 50);
		//computeBetweennessCentrality(true, 50);
		
		File gephiFile = FileUtil.replaceExtension(false,fileCoNLL, 
				"_mode_" + nodeSDcutOff + "_" + arcSDcutOff +
				(linkByDependency ? "_DEP" : "") +
				".gexf");
		
		File logFile = FileUtil.replaceExtension(false, gephiFile, "log");
		
		buildGexfFile(gephiFile, logFile);
		buildMatrixfFile(FileUtil.replaceExtension(false, gephiFile, ".tsv"));
		System.out.println(gephiFile);
		
		GephiExporter.buildPdf = true;
		GephiExporter.buildSvg = false;
		GephiExporter.buildSigma = false;				
		
		GephiExporter.imageExporter(gephiFile, 20);
	}

	public static void mainRepLettersMorphAdorner(String[] args, int nodeSDcutOff, int arcSDcutOff) throws IOException {
		//
		//boolean pruneMatrixMode = 0;

		
		//0: mean
		//1: mean+1SD
		//2: mean+2SD
		//3: mean+3SD
		//...		
		//int nodeSDcutOff = 1;  
		//int arcSDcutOff = 2;		 

		File fileCoNLL = null;
		if (args!=null && args.length>0) {
			fileCoNLL = new File(args[0]);
		}
		else {		
			System.err.println("Args needed");
			return;
		}
				
		//processFileSentence(fileCoNLL);
		processFileMorphAdorner(fileCoNLL);

		printBasicStatistics();
		
		removeLabelsNotInMatrix();		
		printMatrixStatistics(true);
		
		pruneMatrix(nodeSDcutOff, -1);		
		removeLabelsNotInMatrix();
		printMatrixStatistics(true);
		
		pruneMatrix(-1, arcSDcutOff);
		removeLabelsNotInMatrix();
		printMatrixStatistics(false);
		
		//computeBetweennessCentrality(false, 50);
		//computeBetweennessCentrality(true, 50);
		
		File gephiFile = FileUtil.replaceExtension(false,fileCoNLL, 
				"_mode_" + nodeSDcutOff + "_" + arcSDcutOff +
				(pruningBasedOnMean ? "" : "_MeanOff") + 
				(linkByDependency ? "_DEP" : "") +
				".gexf");
		
		File logFile = FileUtil.replaceExtension(false, gephiFile, ".log");
		
		buildGexfFile(gephiFile, logFile);
		buildMatrixfFile(FileUtil.replaceExtension(false, gephiFile, ".tsv"));
		System.out.println(gephiFile);
		
		GephiExporter.imageExporter(gephiFile, 5);
	}
	
	public static void mainRepLettersFromTriplets(File fileTriplets, int nodeSDcutOff, int arcSDcutOff) throws IOException {

		processFileTriplets(fileTriplets);
		
		printBasicStatistics();
		
		removeLabelsNotInMatrix();		
		printMatrixStatistics(true);
		
		pruneMatrix(nodeSDcutOff, -1);		
		removeLabelsNotInMatrix();
		printMatrixStatistics(true);
		
		pruneMatrix(-1, arcSDcutOff);
		removeLabelsNotInMatrix();
		printMatrixStatistics(false);
		
		File gephiFile = FileUtil.replaceExtension(false,fileTriplets, 
				"_mode_" + -1 + "_" + -1 +
				(linkByDependency ? "_DEP" : "") +
				".gexf");
		
		File logFile = FileUtil.replaceExtension(false, gephiFile, ".log");
		
		buildGexfFile(gephiFile, logFile);
		buildMatrixfFile(FileUtil.replaceExtension(false, gephiFile, ".tsv"));
		System.out.println(gephiFile);
		
		GephiExporter.imageExporter(gephiFile, 5);
	}
	
	public static void main(String[] args) throws IOException {		
		
		/*
		if (args!=null && args.length==3) {
			fileCoNLL = new File(args[0]);
			nodeSDcutOff = Integer.parseInt(args[1]);
			arcSDcutOff = Integer.parseInt(args[2]);
		}
		*/
		
		//mainSocioNLP(args);
		
		BLACK_EDGES = true;
		GephiExporter.buildPdf = true;
		GephiExporter.buildSvg = false;
		GephiExporter.buildSigma = false;		
		GephiExporter.REPULSION_STRENGTH = 600000;
		
		
		/*
		String path = "/Users/fedja/Dropbox/CostLetters/sample/stanford/";
		File f = new File(path + "Dury_Hartlib_1628.txt.conll");
		mainRepLettersStanford(new String[]{f.getAbsolutePath()});
		*/
		
		/*
		File[] files = (new File(path)).listFiles();
		for(File f : files) {
			if (f.getName().endsWith(".conll"))
				mainRepLettersStanford(new String[]{f.getAbsolutePath()});
			break;
		}
		*/
		
		int[][] pruningModes = new int[][]{			
			new int[]{-1,-1},
			//new int[]{-1,0},
			//new int[]{0,0},
			//new int[]{0,1},
			new int[]{1,1},
			//new int[]{1,2},
			new int[]{2,2},
		};
		
		pruningBasedOnMean = false;
		linkByDependency = true;
		GephiExporter.directed = linkByDependency;
		
		/*
		String path = "/Users/fedja/Dropbox/CostLetters/sample/MorphAdorner/conll/gephi";
		File[] files = (new File(path)).listFiles();
		for(int[] pm : pruningModes) {
			for(File f : files) {
				if (f.getName().endsWith(".conll")) {
					init();
					mainRepLettersMorphAdorner(new String[]{f.getAbsolutePath()}, pm[0], pm[1]);
					//break;
					//return;
				}
			}
		}*/
		
		String path = "/Users/fedja/Dropbox/CostLetters/sample/MorphAdorner/conll/gephi/";
		File f = new File(path + "Dury_Hartlib_1628.conll");
		mainRepLettersMorphAdorner(new String[]{f.getAbsolutePath()}, -1, -1);
		
		/*
		String path = "/Users/fedja/Dropbox/CostLetters/sample/MorphAdorner/conll/";
		//File f = new File(path + "Dury_Hartlib_1628_syntactic-triples_anaphora_sum.tsv");
		File f = new File(path + "Dury_Hartlib_1628_syntactic-triples_sum.tsv");
		
		mainRepLettersFromTriplets(f, -1, -1);
		*/
	}


	
}

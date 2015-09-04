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

/*
	FIELDS:
	
	0: "id"
*/
	
	
/*
	Pos tags
	
	CC			conjunction, coordinating					and, or, but
	CD			cardinal number								five, three, 13%
	DT			determiner									the, a, these
	EX			existential there							there were six boys
	FW			foreign word								mais
	IN			conjunction, subordinating or preposition	of, on, before, unless
x	JJ			adjective									nice, easy
x	JJR			adjective, comparative						nicer, easier
x	JJS			adjective, superlative						nicest, easiest
	LS			list item marker							 
	MD			verb, modal auxillary						may, should
x	NN			noun, singular or mass						tiger, chair, laughter
x	NNS			noun, plural								tigers, chairs, insects
x	NNP			noun, proper singular						Germany, God, Alice
x	NNPS		noun, proper plural							we met two Christmases ago
	PDT			predeterminer								both his children
	POS			possessive ending							's
	PRP			pronoun, personal							me, you, it
	PRP$		pronoun, possessive							my, your, our
	RB			adverb										extremely, loudly, hard 
	RBR			adverb, comparative							better
	RBS			adverb, superlative							best
	RP			adverb, particle							about, off, up
	SYM			symbol										%
	TO			infinitival to								what to do?
	UH			interjection								oh, oops, gosh
x	VB			verb, base form								think
x	VBZ			verb, 3rd person singular present			she thinks
x	VBP			verb, non-3rd person singular present		I think
x	VBD			verb, past tense							they thought
x	VBN			verb, past participle						a sunken ship
x	VBG			verb, gerund or present participle			thinking is fun
	WDT			wh-determiner								which, whatever, whichever
	WP			wh-pronoun, personal						what, who, whom
	WP$			wh-pronoun, possessive						whose, whosever
	WRB			wh-adverb									where, when 
*/

public class Lynching_Stanford_Analysis {
	
	static final Color[] colorsPos = new ColorImpl[]{
			new ColorImpl(0,255,0), 	// green adj (J)
			new ColorImpl(0,0,255), 	// blue nouns (N)
			new ColorImpl(255,0,0), 	// red verbs (V)
	};
	
	static final Color blackColor = new ColorImpl(0,0,0);
	
	static final char ADJECTIVE_POS = 'J';	
	static final char NOUN_POS = 'N';
	static final char VERB_POS = 'V';
	
	//subj
	//nsubjpass  His "body" was pierced
	//static final String SBJ_RELATION = ""; 

	// coMatrix[w1][w2] exists if w1<w2 (in alphabetical order)
	// it contains the number of time w1 is in proximity of w2 (before or after a window size of words)
	
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
	
	static final char[] posGroupLabels = new char[]{
		ADJECTIVE_POS, //'J'
		NOUN_POS, //'N',
		VERB_POS //'V'
	};
	
	/*
		acl:relcl[89]
		csubjpass[3]
		aux[8]
		conj[342]
		acl[82]
		appos[54]
		dep[80]
		xcomp[124]
		advmod[14]
		ccomp[117]
		nmod:tmod[40]
		advcl[145]
	x	nsubj[255]
		case[5]
		nmod:poss[44]
		csubj[12]
	x	nsubjpass[128]
		nmod:npmod[12]
		nummod[3]
		nmod[861]
		parataxis[13]
		amod[280]
		compound[486]
		mwe[2]
	x	dobj[271]
	*/
	
	static final HashSet<String> excludedLemmas = new HashSet<String>(
			Arrays.asList(new String[]{"be", "have", "unknown"}));
	
	final static String[] displayedDepRel = new String[]{
		"nsubj",
		"nsubjpass",
		"dobj",
	};
	
	
	final static int maxSD = 5;
	
	static int ROOT_INDEX = 0;
	static int HEAD_BASE_INDEX = 1;
	
	static HashMap<String, int[]> lemma_freq = new HashMap<String, int[]>();
	static HashMap<String, int[]> pos_freq = new HashMap<String, int[]>();
	static HashMap<String, int[]> depRel_freq = new HashMap<String, int[]>();
	static HashMap<LemmaPos, int[]> token_freq = new HashMap<LemmaPos, int[]>();
	static HashBasedTable<LemmaPos, LemmaPos, int[]> finalMatrix = HashBasedTable.create();
	static int numberOfSentences;
	static DescriptiveStatistics nodeFreqStats, arcWeightsStats;
	
	
	
	// coMatrix[w1][w2] exists if w1<w2 (in alphabetical order)
	// it contains the number of time w1 is in proximity of w2 (before or after a window size of words)
	
	static boolean linkByDependency = false;

	// coMatrix[w1][w2] exists if w1<w2 (in alphabetical order)
	// it contains the number of time w1 is in proximity of w2 (before or after a window size of words)
	
	static {
		Arrays.sort(selectedPoS);
		Arrays.sort(displayedDepRel);
	}




	static void processFileSentence(File fileConll) throws FileNotFoundException, IOException {
		System.out.println("Processing file: " + fileConll);
		
		Scanner scan = new Scanner(fileConll);		
		
		while(true) {
			Connl_Stanford snt = Connl_Stanford.getNextConnlLinesSentence(scan);
			numberOfSentences++;
			if (snt==null)
				break;
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
	
	private static Character resolvePosGroup(String pos) {
		if (Arrays.binarySearch(selectedPoS, pos)>=0) {
			return pos.charAt(0);
		}
		return null;
	}

	
	
	
	/*
	static void processFileWindow(File file, int word_window_size) throws FileNotFoundException, IOException {
		System.out.println("Processing file: " + file);
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
		System.out.println("Building Co-Matrix Simmetric");		
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
		System.out.println("Processing file: " + fileConll);
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
		LemmaPos first = null, second = null;
		while(iterFirst.hasNext()) {			
			first = iterFirst.next().getLemmaPos();
			iterSecond = token_sentence.listIterator(iterFirst.nextIndex());
			while(iterSecond.hasNext()) {
				second = iterSecond.next().getLemmaPos();
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
			
			LemmaPos currentTokenLemmaPos = currentToken.getLemmaPos(); //getLemmaPosDepRel()
			LemmaPos headTokenLemmaPos = token_sentence.get(head - HEAD_BASE_INDEX).getLemmaPos(); //getLemmaPosDepRel()
			
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

	private static void increaseInMatrix(LemmaPos first, LemmaPos second) {
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

	private static void pruneMatrix(int nodeSDcutOff, int arcSDcutOff) {
		System.out.println("\n" + "------ PRUNING MATRIX -------");
		double nodeFreqThreshold = 0;
		double arcWeightsThreshold = 0;
		
		if (nodeSDcutOff==-1) 
			System.out.println("Prune mode nodes: freq > 0 (no pruning)");
		else {
			nodeFreqThreshold = nodeFreqStats.getMean() + nodeFreqStats.getStandardDeviation()*nodeSDcutOff;
			System.out.println("Prune mode nodes: freq > mean" + "+ " + nodeSDcutOff + "SD [" + nodeFreqThreshold + "]");
		}
		
		if (arcSDcutOff==-1)
			System.out.println("Prune mode arcs: weights > 0 (no pruning)");
		else {
			arcWeightsThreshold = arcWeightsStats.getMean() + arcWeightsStats.getStandardDeviation()*arcSDcutOff;
			System.out.println("Prune mode arcs:  weight > mean " + "+ " + arcSDcutOff + "SD [" + arcWeightsThreshold + "]");
		}
		 		
		Iterator<Entry<LemmaPos, int[]>> iterLabelsFreq = token_freq.entrySet().iterator();
		while(iterLabelsFreq.hasNext()) {
			Entry<LemmaPos, int[]> next = iterLabelsFreq.next();
			if (next.getValue()[0]<=nodeFreqThreshold)
				iterLabelsFreq.remove();
		}
		
		Iterator<Cell<LemmaPos, LemmaPos, int[]>> iterMatrix = finalMatrix.cellSet().iterator();		
		while(iterMatrix.hasNext()) {
			Cell<LemmaPos, LemmaPos, int[]> next = iterMatrix.next();
			if (  next.getValue()[0]<=arcWeightsThreshold ||
				! token_freq.containsKey(next.getColumnKey()) ||
				! token_freq.containsKey(next.getRowKey()) ) {
	
				iterMatrix.remove();
			}
		}
	}

	private static void buildGexfFile(File outputFile) {
		
		System.out.println("\n---- BUILDING GEXF FILE: " + outputFile);
		
		int number_of_nodes = 0;
		int number_of_edges = 0;		

		
		Gexf gexf = new GexfImpl();
		Calendar date = Calendar.getInstance();
		
		gexf.getMetadata()
			.setLastModified(date.getTime())
			.setCreator("Federico Sangati")
			.setDescription("Lynching project network visualization");
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
		
		for(Cell<LemmaPos, LemmaPos, int[]> cs : finalMatrix.cellSet()) {
			int weight = cs.getValue()[0];
			
			LemmaPos[] tokens_RC = new LemmaPos[]{cs.getRowKey(), cs.getColumnKey()};
			char[] pos_RC = new char[2];
			Node[] nodes_RC = new Node[2];
			for(int i=0; i<2; i++) {
				LemmaPos token = tokens_RC[i];
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
			
			Edge e = nodes_RC[0].connectTo(nodes_RC[1]);
			e.setColor(getArcColor(pos_RC[0],pos_RC[1]));
			e.setWeight(scaleEdge(weight, maxWeight));
			if (linkByDependency) {
				String depRel = tokens_RC[0].depRel;
				if (Arrays.binarySearch(displayedDepRel, depRel)<0)
					depRel = "";
				e.setLabel(depRel);				
			}
			number_of_edges++;
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
		
		
		
		System.out.println("Numer of nodes: " + number_of_nodes);
		System.out.println("Numer of edges: " + number_of_edges);
		
	}



	private static Color getColorPos(char posType) {
		return colorsPos[Arrays.binarySearch(posGroupLabels,posType)];
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
		return (int) Math.ceil((double)freq/max*20);
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
		
		for(Cell<LemmaPos, LemmaPos, int[]> cs : finalMatrix.cellSet()) {
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
	

	


	public static void main(String[] args) throws IOException {		
		
		//
		//boolean pruneMatrixMode = 0;

		
		//0: mean
		//1: mean+1SD
		//2: mean+2SD
		//3: mean+3SD
		//...		
		int nodeSDcutOff = 1;  
		int arcSDcutOff = 0;
		linkByDependency = true;

		
		String path = "data/Lynching/";
		File fileCoNLL = new File(path + "all_processed_Stanford_CORENLP_output.conll");		
		
		/*
		if (args!=null && args.length==3) {
			fileCoNLL = new File(args[0]);
			nodeSDcutOff = Integer.parseInt(args[1]);
			arcSDcutOff = Integer.parseInt(args[2]);
		}
		*/
		
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
		buildGexfFile(gephiFile);				
	}


	
}

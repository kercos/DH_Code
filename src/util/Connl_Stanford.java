package util;


import java.io.FileNotFoundException;
import java.util.Scanner;


public class Connl_Stanford {

	/*
	0  	 ID  	 Token counter, starting at 1 for each new sentence.
	1  	 FORM  	 Word form or punctuation symbol. 		
	2 	LEMMA 	Lemma or stem (depending on particular data set) of word form, or an underscore if not available.
	3 	POSTAG 	part-of-speech tag, where the tagset depends on the language, or identical to the coarse-grained part-of-speech tag if not available.	
	6 	NER 	
	5 	HEAD 	Head of the current token, which is either a value of ID or zero ('0'). Note that depending on the original treebank annotation, there may be multiple tokens with an ID of zero.
	6 	DEPREL 	Dependency relation to the HEAD. The set of dependency relations depends on the particular language. Note that depending on the original treebank annotation, the dependency relation may be meaningfull or simply 'ROOT'.
	*/
	
	//baseIndex=1;
	//rootIndex=0;
	
	public String[] forms, lemmas, postags, ner, deprels;
	public int[] heads;
	public int length;
	
	public Connl_Stanford(int length) {
		this.length = length;
		forms = new String[length];
		lemmas = new String[length];
		postags = new String[length];
		ner = new String[length];
		heads = new int[length];
		deprels = new String[length];
	}
	
	public Connl_Stanford(String[] form, String[] lemma, String[] postags, 
			String[] ner, int[] heads, String[] deprel) {
		this.forms = form;
		this.lemmas = lemma;
		this.postags = postags;
		this.ner = ner;
		this.heads = heads;
		this.deprels = deprel;
		length = this.forms.length;
	}
	
	public Connl_Stanford(String[] lines) {
		this(lines.length);
		for(int l=0; l<length; l++) {
			String line = lines[l];
			String[] fields = line.split("\t");
			this.forms[l] = removeQuotes(fields[1]);
			this.lemmas[l] = removeQuotes(fields[2]);
			this.postags[l] = removeQuotes(fields[3]);
			this.ner[l] = removeQuotes(fields[4]);
			this.heads[l] = fields[5].equals("_") ? -1 : Integer.parseInt(fields[5]);
			this.deprels[l] = removeQuotes(fields[6]);
		}
	}
	
	public static Connl_Stanford getNextConnlLinesSentence(Scanner connlScan) {
		String[] fields = getNextConnlLinesFields(connlScan);
		if (fields==null)
			return null;
		return new Connl_Stanford(fields);
	}

	
	public static String[] getNextConnlLinesFields(Scanner connlScan) {
		String lines = "";
		String line = "";
			
		while ((line.equals("")) && connlScan.hasNextLine()) { //line.startsWith("#")
			line = connlScan.nextLine();
		};		
		if (line.equals("")) 
			return null;
		lines += line + "\n";		
		while(connlScan.hasNextLine()) {			
			line = connlScan.nextLine();
			if (line.equals("")) 
				break;
			lines += line + "\n";
		}				
		return lines.split("\n");
	}
	
	private static String removeQuotes(String s) {
		if (s.startsWith("\"") && s.endsWith("\""))
			return s.substring(1, s.length()-1);
		return s;
	}

	
	public static void main(String[] args) throws FileNotFoundException {

	}
}

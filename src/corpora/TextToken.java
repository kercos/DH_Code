package corpora;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TextToken {

	public String word, lemma;
	public Character pos;
	public String lemmaPos;
	
	public TextToken(String word, String lemma, Character pos) {
		this.word = word;
		this.lemma = lemma;
		this.pos = pos;		
		if (pos!=null)
			lemmaPos = lemma + "_" + pos;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();		
		 sb.append(word!=null ? word : "_").append('|').
			append(lemma!=null ? lemma : "_").append('|').
			append(pos!=null ? pos : "_");
		return sb.toString();
	}

	public static String toStringWords(List<TextToken> subList, boolean lowerCase) {
		if (subList.isEmpty())
			return "";
		StringBuilder sb = new StringBuilder();
		Iterator<TextToken> iter = subList.iterator();
		String next = iter.next().word;
		sb.append(lowerCase ? next.toLowerCase() : next);
		while(iter.hasNext()) {
			next = iter.next().word;
			sb.append(' ').append(lowerCase ? next.toLowerCase() : next);
		}
		return sb.toString();
	}
	
	public static String toStringLemmasPos(List<TextToken> subList, boolean lowerCase) {
		if (subList.isEmpty())
			return "";
		StringBuilder sb = new StringBuilder();
		Iterator<TextToken> iter = subList.iterator();
		TextToken nextToken = iter.next();
		do {			
			//String word = nextToken.word.replace(' ', '_');
			String lemma = nextToken.lemma.replace(' ', '_');
			sb.append(lowerCase ? lemma.toLowerCase() : lemma);
			sb.append('_').append(nextToken.pos);
			if (!iter.hasNext())
				break;
			sb.append(' '); 
			nextToken = iter.next();
		} while(true);
		return sb.toString();
	}

	public static List<String> getWordList(List<TextToken> span, boolean lowerCase) {
		LinkedList<String> result = new LinkedList<String>();
		for(TextToken t : span) {
			result.add(lowerCase ? t.word.toLowerCase() : t.word);
		}
		return result;
	}
	
	public boolean equals(Object o) {
		if (o instanceof TextToken) {
			TextToken otherToken = (TextToken)o;
			return this.word.equals(otherToken.word) &&
					this.lemma.equals(otherToken.lemma) &&
					this.pos.equals(otherToken.pos);
		}
		return false;
	}
	
	public int hashCode() {
		return this.word.hashCode();
	}
		
}

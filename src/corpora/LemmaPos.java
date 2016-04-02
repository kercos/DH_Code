package corpora;

import java.util.HashSet;


public class LemmaPos {

	String lemma;
	Character pos;
	String depRel; //additional non compulsory fields which doesn't effect equality
	
	public LemmaPos(String lemma, Character pos) {		
		this.lemma = lemma;
		this.pos = pos;		
	}
	
	public LemmaPos(String lemma, Character pos, String depRel) {
		this(lemma, pos);
		this.depRel = depRel;
	}
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();		
		 sb.append(lemma!=null ? lemma : "_").append('|')
		 .append(pos!=null ? pos : "_");
		return sb.toString();
	}
	
	public String lemmaPos() {
		StringBuilder sb = new StringBuilder();		
		 sb.append(lemma!=null ? lemma : "_").append('|')
		 .append(pos!=null ? pos : "_");
		return sb.toString();
	}

	public boolean equals(Object o) {
		if (o instanceof LemmaPos) {
			LemmaPos otherToken = (LemmaPos)o;
			return 	this.lemma.equals(otherToken.lemma) &&
					this.pos.equals(otherToken.pos);					
		}
		return false;
	}
	
	public int hashCode() {
		return this.lemma.hashCode();
	}

	
	public static void main(String[] args) {
		LemmaPos lp = new LemmaPos("lemma",'P');
		LemmaPosDepRel lpd = new LemmaPosDepRel("lemma",'P',"depRel");
		//System.out.println(lp.equals(lpd));
		HashSet<LemmaPos> set = new HashSet<LemmaPos>();
		set.add(lp);
		System.out.println(set.contains(lpd.toLemmaPos()));
	}
	
	
		
}

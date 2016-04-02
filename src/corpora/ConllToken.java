package corpora;


public class ConllToken extends TextToken {

	public String ner;
	public int head;
	public String depRel;
	
	public ConllToken(String word, String lemma, Character pos, String ner, int head, String depRel) {
		
		super(word, lemma, pos);
		this.ner = ner;
		this.head = head;
		this.depRel = depRel;
	}
	
	
	public LemmaPos getLemmaPos() {
		return new LemmaPos(lemma, pos, depRel);
	}
	
	public LemmaPosDepRel getLemmaPosDepRel() {
		return new LemmaPosDepRel(lemma, pos, depRel);
	}	
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();		
		 sb.append(word!=null ? word : "_").append('|').
			append(lemma!=null ? lemma : "_").append('|').
			append(pos!=null ? pos : "_").append('|').
			//append(ner!=null ? ner : "_").append('|').
			append(head).append('|').
			append(depRel!=null ? depRel : "_");
		return sb.toString();
	}

	public boolean equals(Object o) {
		if (o instanceof ConllToken) {
			ConllToken otherToken = (ConllToken)o;
			return this.word.equals(otherToken.word) &&
					this.lemma.equals(otherToken.lemma) &&
					this.pos.equals(otherToken.pos) &&
					//this.ner.equals(otherToken.ner) &&
					this.head==otherToken.head &&
					this.depRel.equals(otherToken.depRel);
		}
		return false;
	}
	
		
}

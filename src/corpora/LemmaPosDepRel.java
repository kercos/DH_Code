package corpora;


public class LemmaPosDepRel extends LemmaPos{
	
	public LemmaPosDepRel(String lemma, Character pos, String depRel) {
		super(lemma, pos, depRel);
	}
	
	public LemmaPos toLemmaPos() {
		return new LemmaPos(lemma, pos, depRel);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();		
		 sb.append(lemma!=null ? lemma : "_").append('|')
			.append(pos!=null ? pos : "_").append('|')
			.append(depRel!=null ? depRel : "_");
		return sb.toString();
	}

	public boolean equals(Object o) {
		if (o instanceof LemmaPosDepRel) {
			LemmaPosDepRel otherToken = (LemmaPosDepRel)o;
			return 	this.lemma.equals(otherToken.lemma) &&
					this.pos.equals(otherToken.pos) &&
					this.depRel.equals(otherToken.depRel);
		}
		return false;
	}
	
	public int hashCode() {
		return this.lemma.hashCode();
	}

	
	
		
}

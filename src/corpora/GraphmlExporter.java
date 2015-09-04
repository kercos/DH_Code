package corpora;

import java.io.PrintWriter;

public class GraphmlExporter {

	
	/*
	<?xml version="1.0" encoding="UTF-8"?>
	<graphml xmlns="http://graphml.graphdrawing.org/xmlns">
	<key attr.name="label" attr.type="string" for="node" id="label"/>
	<key attr.name="Edge Label" attr.type="string" for="edge" id="edgelabel"/>
	<key attr.name="weight" attr.type="double" for="edge" id="weight"/>
	<key attr.name="Edge Id" attr.type="string" for="edge" id="edgeid"/>
	<key attr.name="r" attr.type="int" for="node" id="r"/>
	<key attr.name="g" attr.type="int" for="node" id="g"/>
	<key attr.name="b" attr.type="int" for="node" id="b"/>
	<key attr.name="x" attr.type="float" for="node" id="x"/>
	<key attr.name="y" attr.type="float" for="node" id="y"/>
	<key attr.name="size" attr.type="float" for="node" id="size"/>
	<key attr.name="PoS_Type" attr.type="string" for="node" id="0"/>
	<graph edgedefault="undirected">
	 */	
	final static String header = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n"
			+ "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">" + "\n"
			+ "<key attr.name=\"label\" attr.type=\"string\" for=\"node\" id=\"label\"/>" + "\n"
			+ "<key attr.name=\"Edge Label\" attr.type=\"string\" for=\"edge\" id=\"edgelabel\"/>" + "\n"
			+ "<key attr.name=\"weight\" attr.type=\"double\" for=\"edge\" id=\"weight\"/>" + "\n"
			+ "<key attr.name=\"Edge Id\" attr.type=\"string\" for=\"edge\" id=\"edgeid\"/>" + "\n"
			+ "<key attr.name=\"size\" attr.type=\"float\" for=\"node\" id=\"size\"/>" + "\n"
			+ "<key attr.name=\"PoS_Type\" attr.type=\"string\" for=\"node\" id=\"0\"/>" + "\n"
			+ "<graph edgedefault=\"undirected\">";
	
	/*
	</graph>
	</graphml>
	*/
	final static String footer = 
			"</graph>" + "\n"
			+ "</graphml>";
	
	PrintWriter pw;
	
	public GraphmlExporter(PrintWriter pw) {
		this.pw = pw;
		pw.println(header);
	}
	
	/*
	<node id="comunale_A">
	<data key="label">comunale</data>
	<data key="0">A</data>
	<data key="size">15.0</data>
	</node>
	*/
	public void printNode(String id, String word, char pos, float size) {
		pw.println("<node id=" + quote(id) + ">");
		pw.println("<data key=\"label\">" + word + "</data>");
		pw.println("<data key=\"0\">" + pos + "</data>");
		pw.println("<data key=\"size\">" + size + "</data>");
		pw.println("</node>");
	}
	
	/*
	<edge source="comunale_A" target="assessore_S">
	<data key="edgeid">661a2e39-43ec-4060-924b-d2f348a4270c</data>
	<data key="weight">2.0</data>
	</edge>
	*/
	public void printEdge(String source, String target, float weight) {
		pw.println("<edge source=" + quote(source) + " target=" + quote(target) + ">");
		pw.println("<data key=\"edgeid\">" + source + "|" + target + "</data>");
		pw.println("<data key=\"weight\">" + weight + "</data>");
		pw.println("</edge>");
	}
	
	public String quote(String s) {
		return '"' + s + '"';
	}
	
	public void close() {
		pw.println(footer);
		pw.close();
	}
	

	

	
	
	
	
}

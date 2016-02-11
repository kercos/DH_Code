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
import it.uniroma1.dis.wsngroup.gexf4j.core.viz.NodeShape;

import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.SVGExporter;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.AutoLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.openide.util.Lookup;

import uk.ac.ox.oii.sigmaexporter.SigmaExporter;
import uk.ac.ox.oii.sigmaexporter.model.ConfigFile;
import util.FileUtil;
import util.Utility;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table.Cell;

public class WordNetwork {
	
	static final ColorImpl blackColor = new ColorImpl(0,0,0);
	static final ColorImpl blueColor = new ColorImpl(0,128,255);
	
	static HashMap<String, HashSet<String>> finalMatrix;
	static HashMap<String,Node> nodeTable = new HashMap<String,Node>();
	
	static void buildFinalMatrixFromFile(File inputFile) throws FileNotFoundException {
		finalMatrix = new HashMap<String, HashSet<String>>();
		Scanner scan = new Scanner(inputFile);
		while(scan.hasNextLine()) {
			String line = scan.nextLine();
			if (line.isEmpty())
				continue;
			String[] split = line.split("==");
			if (split.length==1)
				continue;
			String word = split[0];
			String[] jumps = split[1].split(",");
			for(String w : jumps) {
				Utility.putInHashMap(finalMatrix, word, w);
			}
		}
	}
	
	static Node createNode(String label, Graph gexfGraph) {
		Node n = nodeTable.get(label);
		if (n==null) {
			n = gexfGraph.createNode(label);
			n.setLabel(label);
			n.setSize(10);
			n.getShapeEntity().setNodeShape(NodeShape.DIAMOND);										
			n.setColor(blueColor);
			nodeTable.put(label, n);
		}
		return n;
	}
	
	private static File exportMatrixToFiles(File gexfFile) throws Exception {
		
		System.out.println("\n---- BUILDING GEXF FILE: " + gexfFile);
		
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
	 	
		for(Entry<String, HashSet<String>> e : finalMatrix.entrySet()) {
			String key = e.getKey();
			Node key_node = createNode(key, gexfGraph);			
			for(String value : e.getValue()) {			
				Node value_node = createNode(value, gexfGraph);
				Edge gexfEdge = key_node.connectTo(value_node);
				gexfEdge.setColor(blackColor);
				gexfEdge.setWeight(3);
			}
		}
		
		
		StaxGraphWriter graphWriter = new StaxGraphWriter();		
		Writer gexfWriter =  new FileWriter(gexfFile, false);
		graphWriter.writeToStream(gexf, gexfWriter, "UTF-8");
				
		return gexfFile;
		
	}
	
	public static void imageExporter(File gexfFile, int seconds) throws IOException {
		//Init a project - and therefore a workspace
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();
		
		//Import first file
	    ImportController importController = Lookup.getDefault().lookup(ImportController.class);	    
	    Container container = importController.importFile(gexfFile);
	    container.getLoader().setEdgeDefault(EdgeDefault.UNDIRECTED);   //Force DIRECTED
	    container.setAutoScale(false);
        container.setAllowAutoNode(false);  //Don't create missing nodes
        
        //Append imported data to GraphAPI
        importController.process(container, new DefaultProcessor(), workspace);
        
        //Layout setting
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        AutoLayout autoLayout = new AutoLayout(seconds, TimeUnit.SECONDS);
        autoLayout.setGraphModel(graphModel);
        ForceAtlas2 atlasLayout = new ForceAtlas2(null);
        //ratio: applies after x% of layout time
        //https://github.com/gephi/gephi/blob/master/modules/LayoutPlugin/src/main/resources/org/gephi/layout/plugin/forceAtlas/Bundle.properties
        //https://github.com/gephi/gephi/blob/master/modules/LayoutPlugin/src/main/resources/org/gephi/layout/plugin/forceAtlas2/Bundle.properties
        AutoLayout.DynamicProperty repulsionStrength = 
        		AutoLayout.createDynamicProperty("ForceAtlas2.scalingRatio.name", new Double(600000.0), 1f);
        AutoLayout.DynamicProperty gravityProperty = 
        		AutoLayout.createDynamicProperty("ForceAtlas2.gravity.name",new Double(2.0), 0f);
        AutoLayout.DynamicProperty adjustBySizeProperty = 
        		AutoLayout.createDynamicProperty("ForceAtlas2.adjustSizes.name", Boolean.TRUE, 0f);                                 
        autoLayout.addLayout(atlasLayout, 1.0f, 
        		new AutoLayout.DynamicProperty[]{
	        		repulsionStrength, 
	        		gravityProperty, 
	        		adjustBySizeProperty, 
        		});
        autoLayout.execute();
        
        //Preview Setting
        PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
		PreviewModel previewModel = previewController.getModel();
		PreviewProperties prop = previewModel.getProperties();
		prop.putValue(PreviewProperty.NODE_OPACITY, new Double(60.0));
		prop.putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
		prop.putValue(PreviewProperty.NODE_LABEL_FONT, new Font("Arial",Font.PLAIN,8));
		prop.putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.TRUE);
		prop.putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(java.awt.Color.BLACK));
		prop.putValue(PreviewProperty.SHOW_EDGES, Boolean.TRUE);
		prop.putValue(PreviewProperty.EDGE_THICKNESS, new Double(1.5));
		//prop.putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(java.awt.Color.BLACK));
		prop.putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(EdgeColor.Mode.ORIGINAL));
		prop.putValue(PreviewProperty.EDGE_OPACITY, 40);		
		prop.putValue(PreviewProperty.EDGE_CURVED, Boolean.TRUE);		
		prop.putValue(PreviewProperty.BACKGROUND_COLOR, java.awt.Color.WHITE);
		prop.putValue(PreviewProperty.MARGIN, 10);
		previewController.refreshPreview();
		
		String outputPath = gexfFile.getParent();
				
		ExportController ec = Lookup.getDefault().lookup(ExportController.class);
		//ec.exportFile(FileUtil.changeExtension(gexfFile, "svg"));
		
		/*
		ec.exportFile(FileUtil.changeExtension(gexfFile, "pdf"));		
		System.out.println("Exported pdf");
		*/
		
		ec.exportFile(FileUtil.changeExtension(gexfFile, "png"));		
		System.out.println("Exported png");
		
		/*
		Writer writer = new PrintWriter(FileUtil.changeExtension(gexfFile, "svg"));
		SVGExporter svg=(SVGExporter)ec.getExporter("svg");		
		ec.exportWriter(writer,svg);
		
		System.out.println("Exported svg");
		*/
		
		/*
		SigmaExporter se = new SigmaExporter(); 		
		se.setWorkspace(workspace);
		ConfigFile cf = new ConfigFile();
		cf.setDefaults();
		se.setConfigFile(cf, outputPath, false);		
		se.execute();
		File srcConfFile = new File("/Users/fedja/Work/Code/JavaCode/DH_Network_Builder/data/confing.json");
		File dstConfFile = new File(outputPath + "/network/config.json");		
		FileUtils.copyFile(srcConfFile, dstConfFile);
		//System.err.println("Copied file to " + dstConfFile);
		System.out.println("Exported sigma");		 
		*/
	}
	
	public static void main(String args[]) throws Exception {
		String workingDir = "/Users/fedja/Dropbox/Public/WordJumps/";
		File inputFile = new File(workingDir + "exported_lists_dict.txt");
		//buildFinalMatrixFromFile(inputFile);
		File gexfFile = FileUtil.changeExtension(inputFile, "gexf");
		//exportMatrixToFiles(gexfFile);
		imageExporter(gexfFile, 60);
	}
	
}

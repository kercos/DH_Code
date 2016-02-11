package corpora;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.concurrent.TimeUnit;

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
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import uk.ac.ox.oii.sigmaexporter.SigmaExporter;
import uk.ac.ox.oii.sigmaexporter.model.ConfigFile;
import util.FileUtil;

public class GephiExporter {
	
	public static boolean buildPdf = true;
	public static boolean buildSvg = true;
	public static boolean buildSigma = true;
	
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
        ForceAtlasLayout atlasLayout = new ForceAtlasLayout(null);
        //ratio: applies after x% of layout time
        //https://github.com/gephi/gephi/blob/master/modules/LayoutPlugin/src/main/resources/org/gephi/layout/plugin/forceAtlas/Bundle.properties
        AutoLayout.DynamicProperty inertiaProperty = 
			AutoLayout.createDynamicProperty("forceAtlas.inertia.name", new Double(0.1), 0f);
        AutoLayout.DynamicProperty repulsionStrength = 
        		AutoLayout.createDynamicProperty("forceAtlas.repulsionStrength.name", new Double(60000.), 0f);
        AutoLayout.DynamicProperty attractionStrength = 
        		AutoLayout.createDynamicProperty("forceAtlas.attractionStrength.name", new Double(10.), 0f);
        AutoLayout.DynamicProperty maxDisplacement = 
        		AutoLayout.createDynamicProperty("forceAtlas.maxDisplacement.name", new Double(10.), 0f);
        AutoLayout.DynamicProperty autoStabilize = 
        		AutoLayout.createDynamicProperty("forceAtlas.freezeBalance.name", Boolean.TRUE, 0f);
        AutoLayout.DynamicProperty autoStabilizeStrength = 
        		AutoLayout.createDynamicProperty("forceAtlas.freezeStrength.name", new Double(80.), 0f);
        AutoLayout.DynamicProperty autoStabilizeSensibility = 
        		AutoLayout.createDynamicProperty("forceAtlas.freezeInertia.name", new Double(0.2), 0f);
        AutoLayout.DynamicProperty gravityProperty = 
        		AutoLayout.createDynamicProperty("forceAtlas.gravity.name",new Double(500.), 0f);
        AutoLayout.DynamicProperty attDistrb = 
        		AutoLayout.createDynamicProperty("forceAtlas.outboundAttractionDistribution.name", Boolean.FALSE, 0f);//True after 10% of layout time
        AutoLayout.DynamicProperty adjustBySizeProperty = 
        		AutoLayout.createDynamicProperty("forceAtlas.adjustSizes.name", Boolean.TRUE, 0f);                                 
        AutoLayout.DynamicProperty speed = 
        		AutoLayout.createDynamicProperty("forceAtlas.gravity.name",new Double(1.), 0f);
        autoLayout.addLayout(atlasLayout, 1.0f, 
        		new AutoLayout.DynamicProperty[]{
        		//inertiaProperty, 
        		repulsionStrength, 
        		//attractionStrength, 
        		//maxDisplacement, 
        		//autoStabilize, 
        		//autoStabilizeStrength, 
        		//autoStabilizeSensibility, 
        		gravityProperty, 
        		//attDistrb, 
        		adjustBySizeProperty, 
        		//speed
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
		
		if (buildPdf) {		
			//ec.exportFile(FileUtil.changeExtension(gexfFile, "svg"));
			ec.exportFile(FileUtil.changeExtension(gexfFile, "pdf"));
		}
		
		if (buildSvg) {
			Writer writer = new PrintWriter(FileUtil.changeExtension(gexfFile, "svg"));
			SVGExporter svg=(SVGExporter)ec.getExporter("svg");		
			ec.exportWriter(writer,svg);
		}
		
		if (buildSigma) {
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
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

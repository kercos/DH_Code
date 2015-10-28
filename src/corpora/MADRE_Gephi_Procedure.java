package corpora;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.concurrent.TimeUnit;

import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.PDFExporter;
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

public class MADRE_Gephi_Procedure {

	public static void main(String[] args) throws IOException {
		//Init a project - and therefore a workspace
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();
		
		//Import first file
	    ImportController importController = Lookup.getDefault().lookup(ImportController.class);	    
	    File file = new File("data/corriere_2011_mode_2_2.gexf");
	    Container container = importController.importFile(file);
	    container.getLoader().setEdgeDefault(EdgeDefault.UNDIRECTED);   //Force DIRECTED
        container.setAllowAutoNode(false);  //Don't create missing nodes
        
        //Append imported data to GraphAPI
        importController.process(container, new DefaultProcessor(), workspace);
        
        //Layout setting
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        AutoLayout autoLayout = new AutoLayout(20, TimeUnit.SECONDS);
        autoLayout.setGraphModel(graphModel);
        ForceAtlasLayout atlasLayout = new ForceAtlasLayout(null);
        AutoLayout.DynamicProperty adjustBySizeProperty = 
        		AutoLayout.createDynamicProperty(
        				"forceAtlas.adjustSizes.name",
        				Boolean.TRUE, 0.1f);//True after 10% of layout time
        AutoLayout.DynamicProperty gravityProperty = 
        		AutoLayout.createDynamicProperty(
        				"forceAtlas.gravity.name",
        				new Double(500.), 0f);
        AutoLayout.DynamicProperty repulsionProperty = 
        		AutoLayout.createDynamicProperty(
        				"forceAtlas.repulsionStrength.name", 
        				new Double(60000.), 0f);
        autoLayout.addLayout(atlasLayout, 1.0f, 
        		new AutoLayout.DynamicProperty[]{adjustBySizeProperty, repulsionProperty, gravityProperty});
        autoLayout.execute();
        
        //Preview Setting
        PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
		PreviewModel previewModel = previewController.getModel();
		PreviewProperties prop = previewModel.getProperties();		
		prop.putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);		
		prop.putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.TRUE);
		prop.putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.BLACK));
		prop.putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.BLACK));
		prop.putValue(PreviewProperty.EDGE_CURVED, Boolean.TRUE);
		prop.putValue(PreviewProperty.EDGE_OPACITY, 40);		
		prop.putValue(PreviewProperty.BACKGROUND_COLOR, Color.WHITE);
		prop.putValue(PreviewProperty.BACKGROUND_COLOR, Color.WHITE);
		previewController.refreshPreview();
				
		//ExportController ec = Lookup.getDefault().lookup(ExportController.class);
		//ec.exportFile(new File("data/output.svg"));		
		//Writer writer = new PrintWriter(new File("data/output.svg"));
		//SVGExporter svg=(SVGExporter)ec.getExporter("svg");
		//ec.exportWriter(writer,svg);
		
		SigmaExporter se = new SigmaExporter(); 		
		se.setWorkspace(workspace);
		ConfigFile cf = new ConfigFile();
		cf.setDefaults();
		se.setConfigFile(cf, "data/", false);
		//se.setConfigFile(ConfigFile cfg, String path, boolean renumber)
		se.execute();
		

	}

}

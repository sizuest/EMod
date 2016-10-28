/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
 *
 * Copyright (c) 2011 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/
package ch.ethz.inspire.emod.gui;


import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.piccolo2d.PCamera;
import org.piccolo2d.event.PMouseWheelZoomEventHandler;
import org.piccolo2d.extras.event.PSelectionEventHandler;
import org.piccolo2d.extras.swt.PSWTCanvas;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.gui.graph.AGraphElement;
import ch.ethz.inspire.emod.gui.graph.AIONode;
import ch.ethz.inspire.emod.gui.graph.ConnectionLine;
import ch.ethz.inspire.emod.gui.graph.GraphElementPosition;
import ch.ethz.inspire.emod.gui.graph.GraphEventHandler;
import ch.ethz.inspire.emod.gui.graph.GraphMidMouseEventHandler;
import ch.ethz.inspire.emod.gui.graph.InputNode;
import ch.ethz.inspire.emod.gui.graph.KeyEventHandler;
import ch.ethz.inspire.emod.gui.graph.MachineComponentGraphElement;
import ch.ethz.inspire.emod.gui.graph.OutputNode;
import ch.ethz.inspire.emod.gui.graph.SimulationControlGraphElement;
import ch.ethz.inspire.emod.gui.utils.MachineComponentHandler;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.simulation.ASimulationControl;
import ch.ethz.inspire.emod.utils.IOConnection;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * @author sizuest
 *
 */

public class ModelGraphGUI extends AGUITab {
	
	private static PSWTCanvas canvasModelGraph;
	private TabFolder tabFolder;
	private static Tree treeComponentDBView, treeInputsDBView, treeMathDBView;
	
	private static ArrayList<MachineComponentGraphElement> machineComponents = new ArrayList<MachineComponentGraphElement>();
	private static ArrayList<SimulationControlGraphElement> simulationControls = new ArrayList<SimulationControlGraphElement>();
	private static ArrayList<ConnectionLine> connectionLines = new ArrayList<ConnectionLine>();
	
	private static PSelectionEventHandler selectionEventHandler;
	
	private static SashForm form;
	
	
	/**
	 * @param parent
	 */
	public ModelGraphGUI(Composite parent) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(1, true));
		init();
	}

 	/**
	 * initialize the layout of the Model GUI
	 */ 	
	private void initLayout() {
		
		// Resizable form
		form = new SashForm(this, SWT.FILL);
	    form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		form.setLayout(new GridLayout(3, false));
		
		
		//set model graph on the left side of the tab model for the machine conifg
		canvasModelGraph = new PSWTCanvas(form, SWT.BORDER);
		canvasModelGraph.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		// Remove standart handlers
		canvasModelGraph.removeInputEventListener(canvasModelGraph.getPanEventHandler());
		canvasModelGraph.removeInputEventListener(canvasModelGraph.getZoomEventHandler());
		
		// Drag handler
		canvasModelGraph.addInputEventListener(new GraphMidMouseEventHandler());
		
		//Add mouse zoom
		final PMouseWheelZoomEventHandler mouseWheelZoomEventHandler = new PMouseWheelZoomEventHandler();
		mouseWheelZoomEventHandler.zoomAboutMouse();
		canvasModelGraph.removeInputEventListener(canvasModelGraph.getZoomEventHandler());
		canvasModelGraph.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseScrolled(MouseEvent e) {
				setZoomLevel(e.count);
			}
		});
		
		//Add Seclection
		selectionEventHandler = new GraphEventHandler(canvasModelGraph.getLayer(), canvasModelGraph.getLayer(), this.getShell());
		canvasModelGraph.addInputEventListener(selectionEventHandler);
		canvasModelGraph.getRoot().getDefaultInputManager().setKeyboardFocus(selectionEventHandler);
		
		// Add key press handler
		canvasModelGraph.addKeyListener(new KeyEventHandler(this));
	

		//set tabfolder on the right side of the tab model for the component DB and for the inputs
		tabFolder = new TabFolder(form, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		initTabCompDB(tabFolder);
		initTabInputs(tabFolder);
		initTabMath(tabFolder);
		
		canvasModelGraph.addControlListener(new ControlListener() {
			
			@Override
			public void controlResized(ControlEvent e) {
				Rectangle bounds = canvasModelGraph.getBounds();
				canvasModelGraph.getCamera().startResizeBounds();
				canvasModelGraph.getCamera().setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
				canvasModelGraph.getCamera().endResizeBounds();
			}
			
			@Override
			public void controlMoved(ControlEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
     }    

	/**
	 * initialize the tab folder on the right side of the Model GUI
	 * @param tabFolder	the tab folder to initialize
	 */ 	
	private void initTabCompDB(TabFolder tabFolder){
		treeComponentDBView = new Tree(tabFolder, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);

		//fill tree with the components existing in the directory
		MachineComponentHandler.fillMachineComponentTree(treeComponentDBView);
		
		//tab for tree item
		TabItem tabCompDBItem = new TabItem(tabFolder, SWT.NONE);
		tabCompDBItem.setText(LocalizationHandler.getItem("app.gui.model.comp"));
		tabCompDBItem.setToolTipText(LocalizationHandler.getItem("app.gui.model.comptooltip"));
		tabCompDBItem.setControl(treeComponentDBView);        
	}

 	/**
	 * update the component db in the tab folder on the right side of the model gui
	 */ 	
	public static void updateTabCompDB(){
		treeComponentDBView.removeAll();
		MachineComponentHandler.fillMachineComponentTree(treeComponentDBView);
	}
	
 	/**
	 * initialize the tab inputs in the tab folder on the right side of the Model GUI
	 * @param tabFolder	the tab folder to initialize
	 */ 	
	private void initTabInputs(TabFolder tabFolder){
		
		//TODO sizuest: create content of tabFolder Inputs
		treeInputsDBView = new Tree(tabFolder, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);

		MachineComponentHandler.fillInputsTree(treeInputsDBView);
		
		// Inputs
		//String[] items = {"ProcessSimulationControl", "StaticSimulationControl", "GeometricKienzleSimulationControl"};
		//for (String name:items){
		//	TreeItem child = new TreeItem(treeInputsDBView, SWT.NONE);
		//	child.setText(name);
		//}
		
		//tab for simulation config
		TabItem tabInputsItem = new TabItem(tabFolder, SWT.NONE);
		tabInputsItem.setText(LocalizationHandler.getItem("app.gui.model.inputs"));
		tabInputsItem.setToolTipText(LocalizationHandler.getItem("app.gui.model.inputstooltip"));
		tabInputsItem.setControl(treeInputsDBView);
	}
	
	private void initTabMath(TabFolder tabFolder){
		treeMathDBView = new Tree(tabFolder, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);

		TabItem tabMathItem = new TabItem(tabFolder, SWT.NONE);
		tabMathItem.setText(LocalizationHandler.getItem("app.gui.model.math"));
		tabMathItem.setToolTipText(LocalizationHandler.getItem("app.gui.model.mathtooltip"));
		tabMathItem.setControl(treeMathDBView);
	}
	
	/**
	 * initialize the component tree as drag source
	 * @param treeComponentDBView	the tree to set as drag source
	 */ 
	private void initInputDragSource(final Tree treeInputsDBView){
		//set tree as dragsource for the DnD of the components
		int operations = DND.DROP_COPY;
		final DragSource source = new DragSource(treeInputsDBView, operations);
		
		//SOURCE for drag source:
		//http://www.eclipse.org/articles/Article-SWT-DND/DND-in-SWT.html
		
		//DnD shall transfer text of the selected element
		Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
		source.setTransfer(types);
		
		//create draglistener to transfer text of selected tree element
		source.addDragListener(new DragSourceListener() {
			private TreeItem[] selection = null;
			
			//at drag start, get the selected tree element
			public void dragStart(DragSourceEvent event){
				selection = treeInputsDBView.getSelection();
			}
			
			//set the text of the selected tree element as event data
			public void dragSetData(DragSourceEvent event){
				String text = "";
				for(TreeItem item:selection){
					text += (String)item.getText();	
				}
				event.data = text;
			}
			
			//nothing needs to be done at the end of the drag
			public void dragFinished(DragSourceEvent event) {
				// Not used
			}
		});
	}
	
	
 	/**
	 * initialize the component tree as drag source
	 * @param treeComponentDBView	the tree to set as drag source
	 */ 
	private void initComponentDragSource(final Tree treeComponentDBView){
		//set tree as dragsource for the DnD of the components
		int operations = DND.DROP_COPY;
		final DragSource source = new DragSource(treeComponentDBView, operations);
		
		//SOURCE for drag source:
		//http://www.eclipse.org/articles/Article-SWT-DND/DND-in-SWT.html
		
		//DnD shall transfer text of the selected element
		Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
		source.setTransfer(types);
		
		//create draglistener to transfer text of selected tree element
		source.addDragListener(new DragSourceListener() {
			private TreeItem[] selection = null;
			
			//at drag start, get the selected tree element
			public void dragStart(DragSourceEvent event){
				selection = treeComponentDBView.getSelection();
				
				//if a category of components was selected (i.e. a parent of the tree), don't do anything
				for(TreeItem item:selection){
					if(item.getParentItem() == null){
						event.doit = false;
					}
				}
				
			}
			
			//set the text of the selected tree element as event data
			public void dragSetData(DragSourceEvent event){
				String text = "";
				for(TreeItem item:selection){
					
					text += (String)item.getParentItem().getText()+"_"+(String)item.getText();	
				}
				event.data = text;
			}
			
			//nothing needs to be done at the end of the drag
			public void dragFinished(DragSourceEvent event) {
				// Not used
			}
		});
	}

 	/**
	 * initialize the machine table as drop target
	 * @param tableModelView	the table to set as drop target
	 */ 		
	private void initDropTarget(final PSWTCanvas modelGraph){
		//set table as drop target
		int operations = DND.DROP_COPY;
		DropTarget target = new DropTarget(modelGraph, operations);
		
		//SOURCE for drop target:
		//http://www.eclipse.org/articles/Article-SWT-DND/DND-in-SWT.html
		
		//only accept texttransfer
		final TextTransfer textTransfer = TextTransfer.getInstance();
		Transfer[] types = new Transfer[] {textTransfer};
		target.setTransfer(types);
		
		//add drop listener to the target
		target.addDropListener(new DropTargetListener(){
			//show copy icon at mouse pointer
			public void dragEnter(DropTargetEvent event){
				event.detail = DND.DROP_COPY;
			}
			public void dragOver(DropTargetEvent event){
				// Not used
			}
			public void dragLeave(DropTargetEvent event){
				// Not used
			}
			public void dragOperationChanged(DropTargetEvent event) {
				// Not used
			}
			public void dropAccept(DropTargetEvent event){
				// Not used
			}
			//only action is required when element is dropped
			public void drop(DropTargetEvent event){
				//collect string of drag
				String string = null;
		        string = (String) event.data;
		        
		        //get position of the drop		        
				Point p = event.display.map(null, modelGraph, event.x, event.y);
				//Point p = event.display.map(modelGraph, canvasModelGraph, event.x, event.y);
				
				System.out.println(p.toString());
		        
		        if (string.contains("SimulationControl")){
					//create new simulation control
		        	System.out.println("simulationcontrol " + string);
		        	
		    		final String path = PropertiesHandler.getProperty("app.MachineComponentDBPathPrefix") + "/";
		    		Path source = Paths.get(path + "/SimulationControl/" + string + "_Example.xml");
		    	    Path target = Paths.get(PropertiesHandler.getProperty("app.MachineDataPathPrefix") + "/" +
                            				PropertiesHandler.getProperty("sim.MachineName") + "/" +
                            				"MachineConfig/" +
                            				PropertiesHandler.getProperty("sim.MachineConfigName") +
                            				"/", string + "_" + string + ".xml");
		    	    //overwrite existing file, if exists
		    	    CopyOption[] options = new CopyOption[]{
		    	    	StandardCopyOption.REPLACE_EXISTING,
		    	    	StandardCopyOption.COPY_ATTRIBUTES
		    	    }; 
		    	    try {
		    	    	Files.copy(source, target, options);
					} catch (IOException e) {
						e.printStackTrace();
					}
		        	
		        	
					final ASimulationControl sc = Machine.addNewInputObject(string, new SiUnit(Unit.NONE));
					
					//add the machine component to the table
					addGraphItem(sc, new GraphElementPosition(p, canvasModelGraph));
		        }
				
				else if(string.contains("ThermalTest")){
					return;
				}
				else if(string.contains("SimulationControl")){
					return;
				}
				
		        else{	        	
		        	final String[] split = string.split("_",2);
			        split[1] = split[1].replace(".xml","");
			        
			        //create new machine component out of component and type
			        final MachineComponent mc = Machine.addNewMachineComponent(split[0], split[1]);
			        
			        if(null == mc)
			        	return;
			        
			        //add the machine component to the table
			        addGraphItem(mc, new GraphElementPosition(p, canvasModelGraph));
		        }


			}
		});	
	}
	
	/**
	 * add new simulation control to the machine table
	 * @param sc	simulation control to add to the machine table
	 * @param p 
	 */ 	
	public static void addGraphItem(final ASimulationControl sc, GraphElementPosition p){
        SimulationControlGraphElement composite = new SimulationControlGraphElement(sc);
        
        simulationControls.add(composite);
        canvasModelGraph.getLayer().addChild(composite);
        
        if(null==p)
        	p = new GraphElementPosition(0, 0);
        
		composite.setOffset(p.get());
		

	}
	
	/**
	 * add new simulation control to the machine table
	 * @param sc	simulation control to add to the machine table
	 */ 	
	public static void addGraphItem(final ASimulationControl sc){
        addGraphItem(sc, sc.getPosition());
	}
	
 	/**
	 * add new machine component to the machine table
	 * @param mc	Machine Component to add to the machine table
 	 * @param p 
	 */ 	
	public static void addGraphItem(final MachineComponent mc, GraphElementPosition p){
		MachineComponentGraphElement composite = new MachineComponentGraphElement(mc);
		
		machineComponents.add(composite);
		
		canvasModelGraph.getLayer().addChild(composite);
		composite.setOffset(p.get());
	}
	
	/**
	 * add new machine component to the machine table
	 * @param mc	Machine Component to add to the machine table
	 */ 
	public static void addGraphItem(final MachineComponent mc){
		addGraphItem(mc, mc.getPosition());
	}

 	/**
	 * reset the width of all columns of the machine table
	 * needed after deleting all items
	 */ 	
	public static void updateGraph(){
		//TODO
	}

 	/**
	 * clear all items of the machine table
	 */ 	
	public static void clearGraph() {
		//TODO
	}

	@Override
	public void init() {
		initLayout();
		initComponentDragSource(treeComponentDBView);
		initInputDragSource(treeInputsDBView);	
		initDropTarget(canvasModelGraph);
	}

	@Override
	public void update() {
		updateTabCompDB();	
		initTabCompDB(tabFolder);
		
		for(MachineComponentGraphElement ge: machineComponents)
			canvasModelGraph.getLayer().removeChild(ge);
		
		for(SimulationControlGraphElement ge: simulationControls)
			canvasModelGraph.getLayer().removeChild(ge);
		
		ArrayList<MachineComponent> mclist = Machine.getInstance().getMachineComponentList();
		List<ASimulationControl> sclist = Machine.getInstance().getInputObjectList();

		for(MachineComponent mc:mclist){
			ModelGraphGUI.addGraphItem(mc);
		}
		for(ASimulationControl sc:sclist){
			ModelGraphGUI.addGraphItem(sc);
		}
		
		ModelGraphGUI.redrawConnections();
	}
	

	
	private static void setZoomLevel(int level){
		PCamera camera = canvasModelGraph.getCamera();

        camera.setViewScale(camera.getViewScale()+level * 0.01);
	}
	
	/**
	 * Sets the zoom level, such that all elements are visible
	 */
	public void showAll(){
		double xMin = Double.POSITIVE_INFINITY, 
			   xMax = Double.NEGATIVE_INFINITY, 
			   yMin = Double.POSITIVE_INFINITY,
			   yMax = Double.NEGATIVE_INFINITY;
		
		for(MachineComponentGraphElement mc: machineComponents){
			xMin = Math.min(xMin, mc.getGlobalFullBounds().x);
			xMax = Math.max(xMax, mc.getGlobalFullBounds().x+mc.getGlobalFullBounds().width);
			yMin = Math.min(yMin, mc.getGlobalFullBounds().y);
			yMax = Math.max(yMax, mc.getGlobalFullBounds().y+mc.getGlobalFullBounds().height);
		}
		
		for(SimulationControlGraphElement sc: simulationControls){
			xMin = Math.min(xMin, sc.getGlobalFullBounds().x);
			xMax = Math.max(xMax, sc.getGlobalFullBounds().x+sc.getGlobalFullBounds().width);
			yMin = Math.min(yMin, sc.getGlobalFullBounds().y);
			yMax = Math.max(yMax, sc.getGlobalFullBounds().y+sc.getGlobalFullBounds().height);
		}
		
		canvasModelGraph.getCamera().setViewBounds(new Rectangle2D.Double(xMin, yMin, xMax-xMin, yMax-yMin));
		
	}
	
	public static void drawIOConnection(IOConnection ioc){
		ConnectionLine line = new ConnectionLine(ioc);
		
		connectionLines.add(line);
		canvasModelGraph.getLayer().addChild(line);
	}
	
	public static ArrayList<AIONode> getIONodes(){
		ArrayList<AIONode> ioNodes = new ArrayList<AIONode>();
		
		for(MachineComponentGraphElement mcge: machineComponents)
			ioNodes.addAll(mcge.getIONodes());
		
		for(SimulationControlGraphElement scge: simulationControls)
			ioNodes.addAll(scge.getIONodes());
		
		return ioNodes;
	}

	public static void updateConnections() {
		for(ConnectionLine cl: connectionLines)
			cl.update();
	}

	public static void removeGraphElement(AGraphElement element) {
		
		if(element instanceof MachineComponentGraphElement){
			Machine.removeMachineComponent(((MachineComponentGraphElement) element).getMachineComponent());
			machineComponents.remove(element);
		}
		else if(element instanceof SimulationControlGraphElement){
			Machine.removeInputObject(((SimulationControlGraphElement) element).getSimulationControl());
			simulationControls.remove(element);
		}
		
		redrawConnections();
		
		canvasModelGraph.getLayer().removeChild(element);
	}
	
	public static void redrawConnections() {
		for(ConnectionLine cl: connectionLines)
			canvasModelGraph.getLayer().removeChild(cl);
		
		connectionLines = new ArrayList<ConnectionLine>();
		
		for(IOConnection ioc: Machine.getInstance().getIOLinkList())
			ModelGraphGUI.drawIOConnection(ioc);
		
	}

	public static void removeConnection(ConnectionLine connection){
		Machine.removeIOLink(connection.getIOConnection());
		canvasModelGraph.getLayer().removeChild(connection);
	}
	
	public static void setHighlight(boolean b, IOContainer c){
		for(AIONode node: getIONodes()){
			if(null==c)
				node.setHighlight(b);
			else if (node.getIOObject().getUnit().equals(c.getUnit()) & node.getIOObject().getClass().equals(c.getClass()))
				node.setHighlight(b);
		}
	}
	
	public static void setInputHighlight(boolean b, IOContainer c){
		for(AIONode node: getIONodes()){
			if(node instanceof InputNode)
				if(null==c | (node.getIOObject().getUnit().equals(c.getUnit()) & node.getIOObject().getClass().equals(c.getClass())))
					node.setHighlight(b);
		}
	}
	
	public static void setOutputHighlight(boolean b, IOContainer c){
		for(AIONode node: getIONodes()){
			if(node instanceof OutputNode)
				if(null==c | (node.getIOObject().getUnit().equals(c.getUnit()) & node.getIOObject().getClass().equals(c.getClass())))
					node.setHighlight(b);
		}
	}
	
	public static AIONode getAIONode(Point2D point){
		
		for(AIONode node: getIONodes())
			if( node.getIONode().getGlobalFullBounds().contains(point))
				return node;
		
		return null;
	}

	public static void saveElementPositions(){
		ArrayList<AGraphElement> elements = new ArrayList<AGraphElement>();
		
		elements.addAll(machineComponents);
		elements.addAll(simulationControls);
		
		for(AGraphElement e: elements)
			e.savePosition();
	}
	
	public static Color getIOColor(IOContainer c){
		switch(c.getType()){
		case ELECTRIC:
			return Color.MAGENTA;
		case THERMAL:
			return Color.RED;
		case CONTROL:
			return Color.DARK_GRAY;
		case FLUIDDYNAMIC:
			return Color.BLUE;
		case MECHANIC:
			return Color.GREEN;
		case INFORMATION:
			return Color.YELLOW;
		default:
			return Color.BLACK;
	}
	}	
	
}
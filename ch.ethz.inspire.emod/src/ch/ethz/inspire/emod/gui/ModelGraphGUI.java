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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.piccolo2d.PCamera;
import org.piccolo2d.event.PMouseWheelZoomEventHandler;
import org.piccolo2d.extras.event.PSelectionEventHandler;
import org.piccolo2d.extras.swt.PSWTCanvas;
import org.piccolo2d.util.PBounds;

import ch.ethz.inspire.emod.EModSession;
import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.gui.graph.AGraphElement;
import ch.ethz.inspire.emod.gui.graph.AIONode;
import ch.ethz.inspire.emod.gui.graph.ConnectionLine;
import ch.ethz.inspire.emod.gui.graph.GraphElementPosition;
import ch.ethz.inspire.emod.gui.graph.GraphEventHandler;
import ch.ethz.inspire.emod.gui.graph.GraphMidMouseEventHandler;
import ch.ethz.inspire.emod.gui.graph.IGraphEditable;
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
import ch.ethz.inspire.emod.utils.Defines;
import ch.ethz.inspire.emod.utils.IOConnection;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * @author sizuest
 * 
 */

public class ModelGraphGUI extends AGUITab implements IGraphEditable{

	private static PSWTCanvas canvasModelGraph;
	private TabFolder tabFolder;
	private static Tree treeComponentDBView, treeInputsDBView, treeMathDBView;
	private Button buttonUpdateLibrary;

	private static ArrayList<MachineComponentGraphElement> machineComponents = new ArrayList<MachineComponentGraphElement>();
	private static ArrayList<SimulationControlGraphElement> simulationControls = new ArrayList<SimulationControlGraphElement>();
	private static ArrayList<ConnectionLine> connectionLines = new ArrayList<ConnectionLine>();

	private static PSelectionEventHandler selectionEventHandler;

	private static SashForm form;

	private static final int MIN_WIDTH_MODEL = 300;
	private static final int MIN_WIDTH_LIBRARY = 300;

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

		// set model graph on the left side of the tab model for the machine
		// conifg
		canvasModelGraph = new PSWTCanvas(form, SWT.BORDER);
		canvasModelGraph.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 2, 1));

		// Remove standart handlers
		canvasModelGraph.removeInputEventListener(canvasModelGraph
				.getPanEventHandler());
		canvasModelGraph.removeInputEventListener(canvasModelGraph
				.getZoomEventHandler());

		// Drag handler
		canvasModelGraph.addInputEventListener(new GraphMidMouseEventHandler());

		// Add mouse zoom
		final PMouseWheelZoomEventHandler mouseWheelZoomEventHandler = new PMouseWheelZoomEventHandler();
		mouseWheelZoomEventHandler.zoomAboutMouse();
		canvasModelGraph.removeInputEventListener(canvasModelGraph
				.getZoomEventHandler());
		canvasModelGraph.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseScrolled(MouseEvent e) {
				Point p = e.display.map(canvasModelGraph, canvasModelGraph, e.x, e.y);
				setZoomLevel(p, e.count);
			}
		});

		// Add Seclection
		selectionEventHandler = new GraphEventHandler(
				canvasModelGraph.getLayer(), canvasModelGraph.getLayer(),
				this.getShell());
		canvasModelGraph.addInputEventListener(selectionEventHandler);
		canvasModelGraph.getRoot().getDefaultInputManager()
				.setKeyboardFocus(selectionEventHandler);

		// Add key press handler
		canvasModelGraph.addKeyListener(new KeyEventHandler(this));

		// set tabfolder on the right side of the tab model for the component DB
		// and for the inputs
		Composite library = new Composite(form, SWT.NONE | SWT.NO_TRIM);
		library.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		library.setLayout(new GridLayout(1, true));

		tabFolder = new TabFolder(library, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));

		buttonUpdateLibrary = new Button(library, SWT.PUSH);
		buttonUpdateLibrary.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
				false, false, 1, 1));
		buttonUpdateLibrary.setText("Update");
		buttonUpdateLibrary.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTabCompDB();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {/* Not used */
			}
		});

		initTabCompDB(tabFolder);
		initTabInputs(tabFolder);
		initTabMath(tabFolder);

		canvasModelGraph.addControlListener(new ControlListener() {

			@Override
			public void controlResized(ControlEvent e) {
				updateCanvasSize();
			}

			@Override
			public void controlMoved(ControlEvent e) {/* Not used */
			}
		});

		form.setWeights(new int[] { 1, 0 });

		getShell().addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				updateSashSize();
				updateCanvasSize();
			}
		});
	}

	/**
	 * Update the size of the shash
	 */
	private void updateSashSize() {
		int width = getShell().getClientArea().width;
		int[] weights = form.getWeights();

		if (width >= MIN_WIDTH_MODEL + MIN_WIDTH_LIBRARY) {
			weights[1] = 1000000 * MIN_WIDTH_LIBRARY / width;
			weights[0] = 1000000 - weights[1];
		} else {
			weights[0] = 1000000 * MIN_WIDTH_MODEL
					/ (MIN_WIDTH_MODEL + MIN_WIDTH_LIBRARY);
			weights[1] = 1000000 * MIN_WIDTH_LIBRARY
					/ (MIN_WIDTH_MODEL + MIN_WIDTH_LIBRARY);
		}

		form.setWeights(weights);
	}

	private void updateCanvasSize() {
		Rectangle bounds = canvasModelGraph.getBounds();
		canvasModelGraph.getCamera().startResizeBounds();
		canvasModelGraph.getCamera().setBounds(bounds.x, bounds.y,
				bounds.width, bounds.height);
		canvasModelGraph.getCamera().endResizeBounds();
		canvasModelGraph.redraw();
	}

	/**
	 * initialize the tab folder on the right side of the Model GUI
	 * 
	 * @param tabFolder
	 *            the tab folder to initialize
	 */
	private void initTabCompDB(TabFolder tabFolder) {
		treeComponentDBView = new Tree(tabFolder, SWT.SINGLE | SWT.BORDER
				| SWT.V_SCROLL);

		// fill tree with the components existing in the directory
		MachineComponentHandler.fillMachineComponentTree(treeComponentDBView);

		// tab for tree item
		TabItem tabCompDBItem = new TabItem(tabFolder, SWT.NONE);
		tabCompDBItem
				.setText(LocalizationHandler.getItem("app.gui.model.comp"));
		tabCompDBItem.setToolTipText(LocalizationHandler
				.getItem("app.gui.model.comptooltip"));
		tabCompDBItem.setControl(treeComponentDBView);
	}

	/**
	 * update the component db in the tab folder on the right side of the model
	 * gui
	 */
	public static void updateTabCompDB() {
		treeComponentDBView.removeAll();
		MachineComponentHandler.fillMachineComponentTree(treeComponentDBView);
	}

	/**
	 * initialize the tab inputs in the tab folder on the right side of the
	 * Model GUI
	 * 
	 * @param tabFolder
	 *            the tab folder to initialize
	 */
	private void initTabInputs(TabFolder tabFolder) {

		// TODO sizuest: create content of tabFolder Inputs
		treeInputsDBView = new Tree(tabFolder, SWT.SINGLE | SWT.BORDER
				| SWT.V_SCROLL);

		MachineComponentHandler.fillInputsTree(treeInputsDBView);

		// tab for simulation config
		TabItem tabInputsItem = new TabItem(tabFolder, SWT.NONE);
		tabInputsItem.setText(LocalizationHandler
				.getItem("app.gui.model.inputs"));
		tabInputsItem.setToolTipText(LocalizationHandler
				.getItem("app.gui.model.inputstooltip"));
		tabInputsItem.setControl(treeInputsDBView);
	}

	private void initTabMath(TabFolder tabFolder) {
		treeMathDBView = new Tree(tabFolder, SWT.SINGLE | SWT.BORDER
				| SWT.V_SCROLL);

		TabItem tabMathItem = new TabItem(tabFolder, SWT.NONE);
		tabMathItem.setText(LocalizationHandler.getItem("app.gui.model.math"));
		tabMathItem.setToolTipText(LocalizationHandler
				.getItem("app.gui.model.mathtooltip"));
		tabMathItem.setControl(treeMathDBView);
	}

	/**
	 * initialize the component tree as drag source
	 * 
	 * @param treeComponentDBView
	 *            the tree to set as drag source
	 */
	private void initInputDragSource(final Tree treeInputsDBView) {
		// set tree as dragsource for the DnD of the components
		int operations = DND.DROP_COPY;
		final DragSource source = new DragSource(treeInputsDBView, operations);

		// SOURCE for drag source:
		// http://www.eclipse.org/articles/Article-SWT-DND/DND-in-SWT.html

		// DnD shall transfer text of the selected element
		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
		source.setTransfer(types);

		// create draglistener to transfer text of selected tree element
		source.addDragListener(new DragSourceListener() {
			private TreeItem[] selection = null;

			// at drag start, get the selected tree element
			@Override
			public void dragStart(DragSourceEvent event) {
				selection = treeInputsDBView.getSelection();
			}

			// set the text of the selected tree element as event data
			@Override
			public void dragSetData(DragSourceEvent event) {
				String text = "";
				for (TreeItem item : selection) {
					text += item.getText();
				}
				event.data = text;
			}

			// nothing needs to be done at the end of the drag
			@Override
			public void dragFinished(DragSourceEvent event) {
				// Not used
			}
		});
	}

	/**
	 * initialize the component tree as drag source
	 * 
	 * @param treeComponentDBView
	 *            the tree to set as drag source
	 */
	private void initComponentDragSource(final Tree treeComponentDBView) {
		// set tree as dragsource for the DnD of the components
		int operations = DND.DROP_COPY;
		final DragSource source = new DragSource(treeComponentDBView,
				operations);

		// SOURCE for drag source:
		// http://www.eclipse.org/articles/Article-SWT-DND/DND-in-SWT.html

		// DnD shall transfer text of the selected element
		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
		source.setTransfer(types);

		// create draglistener to transfer text of selected tree element
		source.addDragListener(new DragSourceListener() {
			private TreeItem[] selection = null;

			// at drag start, get the selected tree element
			@Override
			public void dragStart(DragSourceEvent event) {
				selection = treeComponentDBView.getSelection();

				// if a category of components was selected (i.e. a parent of
				// the tree), don't do anything
				for (TreeItem item : selection) {
					if (item.getParentItem() == null) {
						event.doit = false;
					}
				}

			}

			// set the text of the selected tree element as event data
			@Override
			public void dragSetData(DragSourceEvent event) {
				String text = "";
				for (TreeItem item : selection) {

					text += item.getParentItem().getText() + "_"
							+ item.getText();
				}
				event.data = text;
			}

			// nothing needs to be done at the end of the drag
			@Override
			public void dragFinished(DragSourceEvent event) {
				// Not used
			}
		});
	}

	/**
	 * initialize the machine table as drop target
	 * 
	 * @param tableModelView
	 *            the table to set as drop target
	 */
	private void initDropTarget(final PSWTCanvas modelGraph) {
		// set table as drop target
		int operations = DND.DROP_COPY;
		DropTarget target = new DropTarget(modelGraph, operations);

		// SOURCE for drop target:
		// http://www.eclipse.org/articles/Article-SWT-DND/DND-in-SWT.html

		// only accept texttransfer
		final TextTransfer textTransfer = TextTransfer.getInstance();
		Transfer[] types = new Transfer[] { textTransfer };
		target.setTransfer(types);

		// add drop listener to the target
		target.addDropListener(new DropTargetListener() {
			// show copy icon at mouse pointer
			@Override
			public void dragEnter(DropTargetEvent event) {
				event.detail = DND.DROP_COPY;
			}

			@Override
			public void dragOver(DropTargetEvent event) {
				// Not used
			}

			@Override
			public void dragLeave(DropTargetEvent event) {
				// Not used
			}

			@Override
			public void dragOperationChanged(DropTargetEvent event) {
				// Not used
			}

			@Override
			public void dropAccept(DropTargetEvent event) {
				// Not used
			}

			// only action is required when element is dropped
			@Override
			public void drop(DropTargetEvent event) {
				// collect string of drag
				String string = null;
				string = (String) event.data;

				// get position of the drop
				Point p = event.display.map(null, modelGraph, event.x, event.y);
				// Point p = event.display.map(modelGraph, canvasModelGraph,
				// event.x, event.y);

				if (string.contains("SimulationControl")) {
					// create new simulation control
					System.out.println("simulationcontrol " + string);

					final String path = PropertiesHandler.getProperty("app.MachineComponentDBPathPrefix")+ "/";
					Path source = Paths.get(path + "/SimulationControl/"
							+ string + "_Example.xml");
					Path target = Paths.get(
							PropertiesHandler
									.getProperty("app.MachineDataPathPrefix")
									+ "/"
									+ PropertiesHandler
											.getProperty("sim.MachineName")
									+ "/"
									+ "MachineConfig/"
									+ PropertiesHandler
											.getProperty("sim.MachineConfigName")
									+ "/", string + "_" + string + ".xml");
					// overwrite existing file, if exists
					CopyOption[] options = new CopyOption[] {
							StandardCopyOption.REPLACE_EXISTING,
							StandardCopyOption.COPY_ATTRIBUTES };
					try {
						Files.copy(source, target, options);
					} catch (IOException e) {
						e.printStackTrace();
					}

					final ASimulationControl sc = Machine.addNewInputObject(
							string, new SiUnit(Unit.NONE));

					// add the machine component to the table
					addGraphItem(sc, new GraphElementPosition(p,
							canvasModelGraph));
				}

				else if (string.contains("ThermalTest")) {
					return;
				} else if (string.contains("SimulationControl")) {
					return;
				}

				else {
					final String[] split = string.split("_", 2);
					split[1] = split[1].replace(".xml", "");

					// create new machine component out of component and type
					final MachineComponent mc = Machine.addNewMachineComponent(
							split[0], split[1]);

					if (null == mc)
						return;

					// add the machine component to the table
					addGraphItem(mc, new GraphElementPosition(p,
							canvasModelGraph));
				}

			}
		});
	}

	/**
	 * add new simulation control to the machine table
	 * 
	 * @param sc
	 *            simulation control to add to the machine table
	 * @param p
	 */
	public static void addGraphItem(final ASimulationControl sc,
			GraphElementPosition p) {
		SimulationControlGraphElement composite = new SimulationControlGraphElement(
				sc);
		
		

		simulationControls.add(composite);
		canvasModelGraph.getLayer().addChild(composite);

		if (null == p)
			p = new GraphElementPosition(0, 0);
	

		composite.setOffset(p.get());

	}

	/**
	 * add new simulation control to the machine table
	 * 
	 * @param sc
	 *            simulation control to add to the machine table
	 */
	public static void addGraphItem(final ASimulationControl sc) {
		addGraphItem(sc, sc.getPosition());
	}

	/**
	 * add new machine component to the machine table
	 * 
	 * @param mc
	 *            Machine Component to add to the machine table
	 * @param p
	 */
	public static void addGraphItem(final MachineComponent mc, GraphElementPosition p) {
		MachineComponentGraphElement composite = new MachineComponentGraphElement(mc);

		machineComponents.add(composite);

		canvasModelGraph.getLayer().addChild(composite);
		composite.setOffset(p.get());
	}

	/**
	 * add new machine component to the machine table
	 * 
	 * @param mc
	 *            Machine Component to add to the machine table
	 */
	public static void addGraphItem(final MachineComponent mc) {
		addGraphItem(mc, mc.getPosition());
	}

	/**
	 * reset the width of all columns of the machine table needed after deleting
	 * all items
	 */
	public static void updateGraph() {
		// TODO
	}

	/**
	 * clear all items of the machine table
	 */
	public static void clearGraph() {
		// TODO
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

		EModStatusBarGUI.getProgressBar().setText("Preparing canvas ...");
		EModStatusBarGUI.getProgressBar().updateProgressbar(0, false);
		
		canvasModelGraph.getLayer().removeAllChildren();
		machineComponents = new ArrayList<MachineComponentGraphElement>();
		simulationControls = new ArrayList<SimulationControlGraphElement>();

		ArrayList<MachineComponent> mclist = Machine.getInstance().getMachineComponentList();
		List<ASimulationControl> sclist = Machine.getInstance().getInputObjectList();
		
		int i = 0, N;
		if(null!=mclist & null!=sclist)
			N = Math.max(1, mclist.size() + sclist.size());
		else if (null!=mclist)
			N = Math.max(1, mclist.size());
		else if (null!=sclist)
			N = Math.max(1, sclist.size());
		else
			N = 1;
			

		EModStatusBarGUI.getProgressBar().setText(
				"Drawing machine elements ...");
		
		if (null!=mclist)
			for (MachineComponent mc : mclist) {
				ModelGraphGUI.addGraphItem(mc);
				EModStatusBarGUI.getProgressBar().updateProgressbar(i++ * 100 / N, false);
			}
		EModStatusBarGUI.getProgressBar().setText("Drawing connections ...");
		if (null!=sclist)
			for (ASimulationControl sc : sclist) {
				ModelGraphGUI.addGraphItem(sc);
				EModStatusBarGUI.getProgressBar().updateProgressbar(i++ * 100 / N, false);
			}

		EModStatusBarGUI.getProgressBar().reset();

		ModelGraphGUI.redrawConnections();
		
		//saveImage();
	}

	private static void setZoomLevel(Point center, int level) {
		PCamera camera = canvasModelGraph.getCamera();
		
		Point2D p = (new GraphElementPosition(center, canvasModelGraph)).get();
		
		if(canvasModelGraph.getCamera().getViewScale()<=2 | level<0)
			camera.scaleViewAboutPoint(1 + level * 0.01/camera.getViewScale(), p.getX(), p.getY());
	}

	
	@Override
	public void showAll() {
		double xMin = Double.POSITIVE_INFINITY, xMax = Double.NEGATIVE_INFINITY, yMin = Double.POSITIVE_INFINITY, yMax = Double.NEGATIVE_INFINITY;

		for (MachineComponentGraphElement mc : machineComponents) {
			xMin = Math.min(xMin, mc.getGlobalFullBounds().x);
			xMax = Math
					.max(xMax,
							mc.getGlobalFullBounds().x
									+ mc.getGlobalFullBounds().width);
			yMin = Math.min(yMin, mc.getGlobalFullBounds().y);
			yMax = Math.max(yMax,
					mc.getGlobalFullBounds().y
							+ mc.getGlobalFullBounds().height);
		}

		for (SimulationControlGraphElement sc : simulationControls) {
			xMin = Math.min(xMin, sc.getGlobalFullBounds().x);
			xMax = Math
					.max(xMax,
							sc.getGlobalFullBounds().x
									+ sc.getGlobalFullBounds().width);
			yMin = Math.min(yMin, sc.getGlobalFullBounds().y);
			yMax = Math.max(yMax,
					sc.getGlobalFullBounds().y
							+ sc.getGlobalFullBounds().height);
		}
		
		PBounds b = new PBounds(xMin, yMin, xMax - xMin, yMax - yMin);

		canvasModelGraph.getCamera().setViewBounds(b);
		
		canvasModelGraph.getCamera().scaleViewAboutPoint(.9, b.getCenterX(), b.getCenterY());
		
		System.out.println(canvasModelGraph.getCamera().getViewScale());
		if(canvasModelGraph.getCamera().getViewScale()>1)
			canvasModelGraph.getCamera().scaleViewAboutPoint(1/canvasModelGraph.getCamera().getViewScale(), b.getCenterX(), b.getCenterY());

	}
	
	/**
	 * Saves the image at the current machine folder
	 */
	@Deprecated
	public void saveImage(){
		try{
			BufferedImage image = new BufferedImage( (int) canvasModelGraph.getCamera().getViewBounds().getWidth(), 
													 (int) canvasModelGraph.getCamera().getViewBounds().getHeight(), 
													 BufferedImage.TYPE_INT_ARGB);
			canvasModelGraph.getLayer().toImage(image, null);
			
			String prefix = EModSession.getRootPath();
			String machineConfig = EModSession.getMachineConfig();
			String path = prefix + File.separator + Defines.MACHINECONFIGDIR + "/" + machineConfig + "/machine.png";
			
			
 			ImageIO.write(image, "png", new File(path));		
 			
 			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Draw the given ioc
	 * @param ioc
	 */
	public static void drawIOConnection(IOConnection ioc) {
		try{
			ConnectionLine line = new ConnectionLine(ioc);
	
			connectionLines.add(line);
			canvasModelGraph.getLayer().addChild(line);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Return all io nodes
	 * @return
	 */
	public static ArrayList<AIONode> getIONodes() {
		ArrayList<AIONode> ioNodes = new ArrayList<AIONode>();

		for (MachineComponentGraphElement mcge : machineComponents)
			ioNodes.addAll(mcge.getIONodes());

		for (SimulationControlGraphElement scge : simulationControls)
			ioNodes.addAll(scge.getIONodes());

		return ioNodes;
	}

	/**
	 * update all lines representing io nodes
	 */
	public static void updateConnections() {
		for (ConnectionLine cl : connectionLines)
			cl.update();
	}

	/**
	 * Remove the given element from the graph
	 * 
	 * @param element
	 */
	public static void removeGraphElement(AGraphElement element) {

		if (element instanceof MachineComponentGraphElement) {
			Machine.removeMachineComponent(((MachineComponentGraphElement) element).getMachineComponent());
			machineComponents.remove(element);
		} else if (element instanceof SimulationControlGraphElement) {
			Machine.removeInputObject(((SimulationControlGraphElement) element).getSimulationControl());
			simulationControls.remove(element);
		}

		redrawConnections();
	}

	/**
	 * redraw all connections
	 */
	public static void redrawConnections() {
		for (ConnectionLine cl : connectionLines)
			canvasModelGraph.getLayer().removeChild(cl);

		connectionLines = new ArrayList<ConnectionLine>();
		
		if(null!=Machine.getInstance().getIOLinkList())
			for (IOConnection ioc : Machine.getInstance().getIOLinkList())
				ModelGraphGUI.drawIOConnection(ioc);

	}

	/**
	 * Remove the given connection
	 * @param connection
	 */
	public static void removeConnection(ConnectionLine connection) {
		Machine.removeIOLink(connection.getIOConnection());
		canvasModelGraph.getLayer().removeChild(connection);
	}

	/**
	 * Highlight the io nodes according to the unit of c
	 * @param b
	 * @param c
	 */
	public static void setHighlight(boolean b, IOContainer c) {
		for (AIONode node : getIONodes()) {
			if (null == c)
				node.setHighlight(b);
			else if (node.getIOObject().getUnit().equals(c.getUnit())
					& node.getIOObject().getClass().equals(c.getClass()))
				node.setHighlight(b);
		}
	}

	/**
	 * Highlight the input nodes according to the unit of c
	 * @param b
	 * @param c
	 */
	public static void setInputHighlight(boolean b, IOContainer c) {
		for (AIONode node : getIONodes()) {
			if (node instanceof InputNode)
				if (null == c
						| (node.getIOObject().getUnit().equals(c.getUnit()) & node
								.getIOObject().getClass().equals(c.getClass())))
					node.setHighlight(b);
		}
	}

	/**
	 * Highlight the output nodes according to the unit of c
	 * @param b
	 * @param c
	 */
	public static void setOutputHighlight(boolean b, IOContainer c) {
		for (AIONode node : getIONodes()) {
			if (node instanceof OutputNode)
				if (null == c
						| (node.getIOObject().getUnit().equals(c.getUnit()) & node
								.getIOObject().getClass().equals(c.getClass())))
					node.setHighlight(b);
		}
	}

	/**
	 * Get the io node at the given position. Return null if no io node is available at the position
	 * 
	 * @param point
	 * @return
	 */
	public static AIONode getAIONode(Point2D point) {

		for (AIONode node : getIONodes())
			if (node.getIONode().getGlobalFullBounds().contains(point))
				return node;

		return null;
	}

	/**
	 * Write the elements graph position to the machine objects
	 */
	public static void saveElementPositions() {
		ArrayList<AGraphElement> elements = new ArrayList<AGraphElement>();

		elements.addAll(machineComponents);
		elements.addAll(simulationControls);

		for (AGraphElement e : elements)
			e.savePosition();
	}

	/**
	 * Get a color according to the io container type
	 * @param c
	 * @return
	 */
	public static Color getIOColor(IOContainer c) {
		switch (c.getType()) {
		case ELECTRIC:
			return Color.GREEN;
		case THERMAL:
			return Color.RED;
		case CONTROL:
			return Color.DARK_GRAY;
		case FLUIDDYNAMIC:
			return Color.BLUE;
		case MECHANIC:
			return Color.ORANGE;
		case INFORMATION:
			return Color.YELLOW;
		default:
			return Color.BLACK;
		}
	}

}
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
package ch.ethz.inspire.emod.dd.gui;

import java.awt.geom.Point2D;

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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.piccolo2d.PCamera;
import org.piccolo2d.event.PMouseWheelZoomEventHandler;
import org.piccolo2d.extras.event.PSelectionEventHandler;
import org.piccolo2d.extras.swt.PSWTCanvas;
import org.piccolo2d.util.PBounds;

import ch.ethz.inspire.emod.dd.Duct;
import ch.ethz.inspire.emod.dd.graph.DuctGraph;
import ch.ethz.inspire.emod.dd.graph.DuctGraphEventHandler;
import ch.ethz.inspire.emod.dd.model.ADuctElement;
import ch.ethz.inspire.emod.dd.model.DuctArc;
import ch.ethz.inspire.emod.dd.model.DuctBypass;
import ch.ethz.inspire.emod.dd.model.DuctDefinedValues;
import ch.ethz.inspire.emod.dd.model.DuctDrilling;
import ch.ethz.inspire.emod.dd.model.DuctElbowFitting;
import ch.ethz.inspire.emod.dd.model.DuctFlowAround;
import ch.ethz.inspire.emod.dd.model.DuctHelix;
import ch.ethz.inspire.emod.dd.model.DuctPipe;
import ch.ethz.inspire.emod.gui.AConfigGUI;
import ch.ethz.inspire.emod.gui.graph.GraphElementPosition;
import ch.ethz.inspire.emod.gui.graph.GraphMidMouseEventHandler;
import ch.ethz.inspire.emod.gui.graph.IGraphEditable;
import ch.ethz.inspire.emod.gui.graph.KeyEventHandler;
import ch.ethz.inspire.emod.gui.utils.ShowButtons;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

/**
 * Composite to edit a duct graphically
 * 
 * @author sizuest
 *
 */
public class DuctConfigGraphGUI extends AConfigGUI implements IGraphEditable{
	private SashForm form;
	private static PSWTCanvas canvas;
	private static DuctGraph ductGraph;
	private static Tree treeDuctDBView;
	private DuctTestingGUI ductTestingGUI;
	private TabFolder tabFolder;
	private Duct duct = new Duct();

	private ADuctElement[] ductElementSelection = { new DuctDrilling(),
			new DuctFlowAround(), new DuctHelix(), new DuctPipe(),
			new DuctElbowFitting(), new DuctArc(), new DuctDefinedValues(),
			new DuctBypass() };

	String name;
	
	private static final int MIN_WIDTH_MODEL = 300;
	private static final int MIN_WIDTH_LIBRARY = 200;
	
	private static PSelectionEventHandler selectionEventHandler;

	/**
	 * DuctConfigGUI
	 * 
	 * @param parent
	 * @param style
	 * @param duct
	 * @param buttons
	 */

	public DuctConfigGraphGUI(Composite parent, int style, Duct duct, int buttons) {
		super(parent, style, buttons);

		this.getContent().setLayout(new GridLayout(1, true));

		tabFolder = new TabFolder(this.getContent(), SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));

		form = new SashForm(tabFolder, SWT.FILL);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		form.setLayout(new GridLayout(3, false));

		this.duct = duct;

		canvas = new PSWTCanvas(form, SWT.BORDER);
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		// Remove standart handlers
		canvas.removeInputEventListener(canvas.getPanEventHandler());
		canvas.removeInputEventListener(canvas.getZoomEventHandler());

		// Drag handler
		canvas.addInputEventListener(new GraphMidMouseEventHandler());

		// Add mouse zoom
		final PMouseWheelZoomEventHandler mouseWheelZoomEventHandler = new PMouseWheelZoomEventHandler();
		mouseWheelZoomEventHandler.zoomAboutMouse();
		canvas.removeInputEventListener(canvas.getZoomEventHandler());
		canvas.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseScrolled(MouseEvent e) {
				Point p = e.display.map(canvas, canvas, e.x, e.y);
				setZoomLevel(p, e.count);
			}
		});
		

		ductGraph = new DuctGraph(duct, null);
		canvas.getLayer().addChild(ductGraph);

		// Add Seclection
		selectionEventHandler = new DuctGraphEventHandler(ductGraph, ductGraph, this.getShell());
		canvas.addInputEventListener(selectionEventHandler);
		canvas.getRoot().getDefaultInputManager().setKeyboardFocus(selectionEventHandler);

		// Add key press handler
		canvas.addKeyListener(new KeyEventHandler(this));
		

		treeDuctDBView = new Tree(form, SWT.BORDER);
		for (ADuctElement e : ductElementSelection) {
			TreeItem childTreeItem = new TreeItem(treeDuctDBView, SWT.NONE);
			childTreeItem.setText(e.getClass().getSimpleName().replace("Duct", ""));
		}

		ductTestingGUI = new DuctTestingGUI(tabFolder, duct);

		TabItem tabDuctDBItem = new TabItem(tabFolder, SWT.NONE);
		tabDuctDBItem.setText(LocalizationHandler
				.getItem("app.dd.config.gui.design"));
		tabDuctDBItem.setControl(form);

		final TabItem tabTestingItem = new TabItem(tabFolder, SWT.NONE);
		tabTestingItem.setText(LocalizationHandler
				.getItem("app.dd.config.gui.analysis"));
		tabTestingItem.setControl(ductTestingGUI);

		tabFolder.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tabFolder.getItem(tabFolder.getSelectionIndex()).equals(
						tabTestingItem))
					updateDuctTestingTable();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Not Used
			}
		});

		updateGraph();

		initDropTarget(canvas, ductGraph);
		initElementDragSource(treeDuctDBView);

		form.setWeights(new int[] { 1, 0 });

		getShell().addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				updateSashSize();
			}
		});
		
		showAll();
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

	
	/**
	 * Open a GUI to edit the given element
	 * @param e
	 * @param d 
	 */
	public void editDuctElementGUI(ADuctElement e, Duct d) {
		Shell shell = EditDuctElementGUI.editDuctElementGUI(this.getShell(), e, d);
		shell.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				updateGraph();
			}
		});

	}

	private void updateDuctTestingTable() {
		ductTestingGUI.update();
	}

	private void updateGraph() {

		canvas.setEnabled(false);
		ductGraph.redraw(duct);
		canvas.setEnabled(true);
	}

	@Override
	public void update() {
		if(this.isDisposed())
			return;
		
		updateGraph();
		
		ductTestingGUI.update();

		this.redraw();
		this.layout();
	}

	private void initElementDragSource(final Tree treeElementDBView) {
		// set tree as dragsource for the DnD of the components
		int operations = DND.DROP_COPY;
		final DragSource source = new DragSource(treeElementDBView, operations);

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
				selection = treeElementDBView.getSelection();
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

	private void initDropTarget(final PSWTCanvas canvasModelGraph, final DuctGraph ductGraphInit) {
		// set table as drop target
		int operations = DND.DROP_COPY;
		DropTarget target = new DropTarget(canvasModelGraph, operations);

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
				Point2D p = (new GraphElementPosition(event.display.map(null, canvasModelGraph, event.x, event.y), canvasModelGraph)).get();
				
				DuctGraph ductGraph = ductGraphInit.getDuctGraph(p);
				int index = ductGraph.getElementIndex(p);
				
				if(index<0 | null==ductGraph)
					return;
				

				ADuctElement e = Duct.newDuctElement(string);

				if (null != e ) {
					addElementAtReceiver(index, e, ductGraph.getDuct());
					updateGraph();
				}
				
				wasEdited();
				
				duct.setRootDuct();

			}
		});
	}

	private void addElementAtReceiver(int index, ADuctElement e, Duct duct) {

		if (duct.getElementsExceptFittings().size() > index) {
			duct.addElement(index, e);
			return;
		}

		duct.addElement(e);
	}

	/**
	 * Open a new duct configurator in the active shell, where the duct is a sub model
	 * of the model type 'type' and parameter set 'parameter'
	 * @param type
	 * @param parameter
	 * @param name
	 */
	public static void editDuctGUI(String type, String parameter, String name) {
		editDuctGUI(Display.getCurrent().getActiveShell(), type + "_"
				+ parameter + "_" + name);
	}

	
	/**
	 * Open a new duct configurator in the given shell, where the duct is a sub model
	 * of the model type 'type' and parameter set 'parameter'
	 * @param parent
	 * @param type
	 * @param parameter
	 * @param name
	 */
	public static void editDuctGUI(Shell parent, String type, String parameter,
			String name) {
		editDuctGUI(parent, type + "_" + parameter + "_" + name);
	}

	
	/**
	 * Open a new duct configurator in the active shell for the given duct name
	 * @param type
	 */
	public static void editDuctGUI(String type) {
		editDuctGUI(Display.getCurrent().getActiveShell(), type);
	}

	
	/**
	 * Open a new duct configurator in the given shell for the given duct name
	 * @param parent
	 * @param type
	 */
	public static void editDuctGUI(Shell parent, String type) {
		final Shell shell = new Shell(parent, SWT.TITLE | SWT.SYSTEM_MODAL | SWT.CLOSE | SWT.MAX | SWT.RESIZE);
		shell.setLayout(new GridLayout(1, true));
		
		DuctConfigGraphGUI gui = new DuctConfigGraphGUI(shell, SWT.NONE, Duct.buildFromDB(type), ShowButtons.CANCEL | ShowButtons.OK);

		shell.setText("DuctDesigner: " + type);
		
		// Icon
		shell.setImages(new Image[] {new Image(Display.getDefault(),"src/resources/icons/DuctDesignerIcon_128x128.png"), 
				                     new Image(Display.getDefault(),"src/resources/icons/DuctDesignerIcon_48x48.png"), 
				                     new Image(Display.getDefault(),"src/resources/icons/DuctDesignerIcon_32x32.png"), 
				                     new Image(Display.getDefault(),"src/resources/icons/DuctDesignerIcon_22x22.png"), 
				                     new Image(Display.getDefault(),"src/resources/icons/DuctDesignerIcon_16x16.png")});


		shell.pack();

		shell.layout();
		shell.redraw();
		shell.open();
		
		gui.showAll();
		gui.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				shell.dispose();
			}
		});
	}

	@Override
	public void save() {
		duct.save();
	}

	@Override
	public void reset() {
		duct.clone(Duct.buildFromDB(this.name));
		//update();
	}

	/**
	 * Set the duct object to be edited
	 * @param duct
	 */
	public void setDuct(Duct duct) {
		// this.duct.clone(duct);
		this.duct = duct;
		this.ductTestingGUI.setDuct(duct);

		update();
	}

	/**
	 * Returns the current tab folder
	 * @return
	 */
	public TabFolder getTabFolder() {
		return this.tabFolder;
	}
	
	private static void setZoomLevel(Point center, int level) {
		PCamera camera = canvas.getCamera();

		Point2D p = (new GraphElementPosition(center, canvas)).get();
		if(canvas.getCamera().getViewScale()<1 | level<0)
			camera.scaleViewAboutPoint(1 + level * 0.01/camera.getViewScale(), p.getX(), p.getY());
	}


	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.gui.graph.IGraphEditable#showAll()
	 */
	@Override
	public void showAll() {
		
		// Minimal bounds
		PBounds b = ductGraph.getGlobalFullBounds();

		canvas.getCamera().setViewBounds(b);
		
		canvas.getCamera().scaleViewAboutPoint(.9, b.getCenterX(), b.getCenterY());
		
		if(canvas.getCamera().getViewScale()>1)
			canvas.getCamera().scaleViewAboutPoint(1/canvas.getCamera().getViewScale(), b.getCenterX(), b.getCenterY());
		
	}

}

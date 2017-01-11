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

package ch.ethz.inspire.emod.dd.graph;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Shell;
import org.piccolo2d.PCamera;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.extras.event.PSelectionEventHandler;
import org.piccolo2d.extras.swt.PSWTPath;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.dd.gui.EditDuctElementGUI;

/**
 * GraphEventHandler
 * 
 * Implements the mouse event handlings for the graph. Those are:
 * - Selection of elements by a marquee 
 * - Selection of a single element by left-click 
 * - Selection of an additional element by CTRL+left-click 
 * - Editing an element by right-click 
 * - Deletion of a selected element by DEL
 * - Drawing of new
 * IOConnections
 * 
 * @author sizuest
 * 
 */
public class DuctGraphEventHandler extends PSelectionEventHandler {

	/* Point where the mouse drag action was initiated */
	private Point2D dragSource;
	/* Line (rect) for the selection marquee */
	private PSWTPath selectionMarquee = new PSWTPath();
	/* Parent graph */
	private DuctGraph parentGraph;
	/* Initiating button */
	private int initMouseButton=-1;
	
	DuctGraphElement selection = null;
	/*
	 * Parent shell, required when opening editor windows of type {@link
	 * AConfigGUI.java}
	 */
	private Shell parentShell;

	/**
	 * Constructor
	 * 
	 * @param marqueeParent
	 *            PNode to add the selection marquee. Attention: This is for
	 *            compability reasons only. The actual marquee is draw by this
	 *            class at not by {@link PSelectionEventHandler} since
	 *            their seams to be a compability issue with SWT. The marquee
	 *            will alway be pointed the PNode raising the mouse drag event
	 * @param selectableParent
	 *            PNode to select the children from
	 * @param parent
	 *            Shell of the parent, required for editor windows with the
	 *            property SWT.APPLICATION_MODAL set
	 */
	public DuctGraphEventHandler(final DuctGraph marqueeParent,
			final DuctGraph selectableParent, final Shell parent) {
		super(marqueeParent, selectableParent);
		this.parentGraph = selectableParent;
		this.parentShell = parent;
	}

	/**
	 * dragActivityFirstStep
	 * 
	 * Handles the initialisation of a new drag. Compared to its parent class,
	 * the following actions are added: - If the drag is initiated over a
	 * AIONode is selected, a line is drawn form the node to the mouse pointer
	 * and all nodes of oposite type (input > output, output > input) are
	 * highlighted - If the drag is initiated over an empty space, a selection
	 * marquee is added.
	 * 
	 * @param event
	 */
	@Override
	protected void dragActivityFirstStep(final PInputEvent event) {

		/*
		 * We only have to care about this action, if it is raised due to a
		 * left-click
		 */
		initMouseButton = event.getButton();
		
		if (initMouseButton == MouseEvent.BUTTON1 | initMouseButton == MouseEvent.BUTTON3) {
			/* Check, if an element has been clicked */
			selection = parentGraph.getSelection(event.getPosition());
			
			if(null!=selection){
				selection.raiseToTop();
				select(selection);
			}
			else
				unselectAll();
		}
		
		if (initMouseButton == MouseEvent.BUTTON1) {

			/* Save the source of the drag event */
			dragSource = event.getCanvasPosition();

			/* If the mouse is over the free area -> draw marquee */
			if (event.getPickedNode() instanceof PCamera) {
				event.getCamera().addChild(selectionMarquee);
			}
		}

	}

	/**
	 * dragActivityStep
	 * 
	 * Handles a mouse drag. Compared to its parent class, the following actions
	 * are added: - If a connection line is to be drawed, the line is updated
	 * according to the mouse position - If a selection marquee is to be drawed,
	 * the line is updated according to the mouse position
	 * 
	 * @param event
	 */
	@Override
	protected void dragActivityStep(final PInputEvent event) {

		/*
		 * Only if their is a mouse drag source different from null, a
		 * connection line needs an update
		 */
		if (null!=selection & initMouseButton==MouseEvent.BUTTON1){
			selection.getDuctGraph().moveElement(selection, event.getPosition());
		}
		/*
		 * Their seams to be an other reason for an update -> let's update the
		 * marquee
		 */
		else
			updateMarquee2(event);

	}

	/**
	 * dragActivityFinalStep
	 * 
	 * Handles the finalization of a mouse drag. Compared to its parent class,
	 * the following actions are added: - Remove node highlightning - Save the
	 * element position to the elements of the {@link Machine.java} class - If a
	 * connection line was drawed, check if a new connection has to be added -
	 * As a result of a right click on a component, open a configuration windows
	 * for the particular component
	 * 
	 * @param event
	 */
	@Override
	protected void dragActivityFinalStep(final PInputEvent event) {
		/* Action is finished -> the line and marquee are not needed anymore */
		selectionMarquee.removeFromParent();
		
		/* Handle drag */
		if(null!=selection  & initMouseButton == MouseEvent.BUTTON1){
			selection.getDuctGraph().moveElement(selection, event.getPosition());
			select(selection);
		}

		/* Rearrange all nodes */
		parentGraph.update();
		

		/*
		 * If the right mouse button is pressed over an element, 
		 * a configuration window shall be opened.
		 * 
		 * Attention: This is done here instead of in dragActivityFirstStep,
		 * since the event is not handled correct otherwise. Their seams to be a
		 * glitch in the selection handling. Even if the node is unselected
		 * (unselectAll()) after the action, a subsequent left-click anywhere in
		 * the graph will select the node again. ZS
		 */
		if (event.getButton() == MouseEvent.BUTTON3 & selection!=null) {
			Shell shell = EditDuctElementGUI.editDuctElementGUI(parentShell, selection.getElement(), selection.getDuctGraph().getDuct());
			shell.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					parentGraph.update();
				}
			});
		}
	}

	/**
	 * keyPressed
	 * 
	 * implements the key actions for all graph elements
	 * 
	 * @param event
	 */
	@Override
	public void keyPressed(final PInputEvent event) {
		super.keyPressed(event);

		switch (event.getKeyCode()) {
		}
	}

	/**
	 * updates the selection marquee according to the mouse event
	 * 
	 * @param event
	 */
	private void updateMarquee2(PInputEvent event) {
		// Nothing to do ...
		if (null == selectionMarquee | null == dragSource)
			return;

		/*
		 * Definitions x0/y0 : Initial drag source h x b : Current size of the
		 * marquee x1/y1 : Current mouse position
		 * 
		 * Calculations: case x0<x1 -> x0 stays coordinate, only b is adapted
		 * else -> x1 becomes new marquee coordinate and b is adapted
		 * 
		 * same for y,h
		 */
		double x0 = dragSource.getX(), y0 = dragSource.getY(), h, b, x1 = event
				.getCanvasPosition().getX(), y1 = event.getCanvasPosition()
				.getY();

		/* Determine new marquee rectangle */
		if (x0 < x1)
			b = x1 - x0;
		else {
			b = x0 - x1;
			x0 = x1;
		}

		if (y0 < y1)
			h = y1 - y0;
		else {
			h = y0 - y1;
			y0 = y1;
		}

		/* New marquee line */
		selectionMarquee.setPathToPolyline(new float[] { (float) x0,
				(float) (x0 + b), (float) (x0 + b), (float) x0, (float) x0 },
				new float[] { (float) y0, (float) y0, (float) (y0 + h),
						(float) (y0 + h), (float) y0 });

		/* Color properties */
		selectionMarquee.setStrokeColor(Color.BLACK);
		selectionMarquee.setPaint(Color.GRAY);
		selectionMarquee.setTransparency(0.5f);

		/* Adapt scale to camera */
		selectionMarquee.setScale(event.getCamera().getScale());
	}
}

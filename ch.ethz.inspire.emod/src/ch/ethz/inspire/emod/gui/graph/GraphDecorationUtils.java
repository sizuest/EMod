package ch.ethz.inspire.emod.gui.graph;

import java.awt.Color;

import org.piccolo2d.extras.nodes.PComposite;
import org.piccolo2d.extras.swt.PSWTPath;

/**
 * Implements various functions for object decoration in graphs
 * @author simon
 *
 */
public class GraphDecorationUtils {
	
	/**
	 * Creates a PComposite containing the shadow of the given shape with the given color
	 * and adds it to the given parent
	 * 
	 * @param parent
	 * @param shape
	 * @param color
	 * @return
	 */
	public static PComposite addShadow(PComposite parent, PSWTPath shape, Color color){
		
		PComposite container = new PComposite();
		
		for(int i=0; i<10; i++){
			final PSWTPath shadow = PSWTPath
					.createRoundRectangle(
							(float)shape.getX()+5+i,
							(float)shape.getY()+5+i,
							(float) (shape.getWidth())   - 2*i,
							(float) (shape.getHeight())  - 2*i, 
							10f-i, 10f-i);
			shadow.setPaint(color);
			shadow.setTransparency(.1f+i*i/100f);
			container.addChild(shadow);
		}
		
		container.setOffset(shape.getOffset());
		
		//container.setOffset(shape.getOffset().getX()+10, shape.getOffset().getY()+10);
		
		parent.addChild(container);
		shape.raiseAbove(container);
		
		return container;
	}
	
	/**
	 * Creates a PComposite containing the gloom of the given shape with the given color
	 * and adds it to the given parent
	 * @param parent
	 * @param shape
	 * @param color
	 * @return
	 */
	public static PComposite addGloomToRectangle(PComposite parent, PSWTPath shape, Color color){
		
		int size = 5;
		
		PComposite container = new PComposite();
		
		for(int i=0; i<size; i++){
			final PSWTPath gloom = PSWTPath
					.createRoundRectangle(
							(float)shape.getX()+i-size,
							(float)shape.getY()+i-size,
							(float) (shape.getWidth())  + 2*size - 2*i,
							(float) (shape.getHeight()) + 2*size - 2*i, 
							5f-i, 5f-i);
			gloom.setPaint(color);
			gloom.setStrokeColor(color);
			gloom.setTransparency(.05f+i/100f);
			
			
			container.addChild(gloom);
		}
		
		// Add container to parent below reference shape
		int idx = 0;
		while(idx<parent.getChildrenCount() & !(parent.getChild(idx).equals(shape)))
			idx++;
		parent.addChild(idx, container);
		
		
		container.setOffset(shape.getOffset());
		
		return container;
	}

}

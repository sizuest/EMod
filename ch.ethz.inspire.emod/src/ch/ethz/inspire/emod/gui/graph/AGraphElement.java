package ch.ethz.inspire.emod.gui.graph;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import org.piccolo2d.extras.nodes.PComposite;

import ch.ethz.inspire.emod.gui.ModelGraphGUI;

public abstract class AGraphElement extends PComposite{

	private static final long serialVersionUID = 1L;
	
	public abstract void savePosition();
	public abstract ArrayList<AIONode> getIONodes();
	
	public AGraphElement() {
		super();
		
		this.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent arg0) {
				ModelGraphGUI.updateConnections();
			}
		});
		
	}
	
	@Override
	public void removeFromParent() {
		ModelGraphGUI.removeGraphElement(this);
    }

}

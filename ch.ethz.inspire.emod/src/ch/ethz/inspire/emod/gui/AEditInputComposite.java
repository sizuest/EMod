package ch.ethz.inspire.emod.gui;

import org.eclipse.swt.widgets.Composite;

import ch.ethz.inspire.emod.gui.utils.ShowButtons;
import ch.ethz.inspire.emod.simulation.ASimulationControl;

public abstract class AEditInputComposite extends AConfigGUI{
	
	protected Composite parent;
	protected ASimulationControl sc;

	public AEditInputComposite(Composite parent, int style, ASimulationControl sc) {
		super(parent, style, ShowButtons.NONE);
		
		this.parent = parent;
		this.sc = sc;
		
		init();
	}
	
	public abstract void init();
	
	public abstract void save();
	
	public abstract void reset();

}

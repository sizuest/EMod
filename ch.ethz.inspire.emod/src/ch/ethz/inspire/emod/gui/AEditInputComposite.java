package ch.ethz.inspire.emod.gui;

import org.eclipse.swt.widgets.Composite;

import ch.ethz.inspire.emod.gui.utils.ShowButtons;
import ch.ethz.inspire.emod.simulation.ASimulationControl;

/**
 * Generic composite to edit simulation inputs
 * @author sizuest
 *
 */
public abstract class AEditInputComposite extends AConfigGUI {

	protected Composite parent;
	protected ASimulationControl sc;

	/**
	 * @param parent
	 * @param style
	 * @param sc
	 */
	public AEditInputComposite(Composite parent, int style,
			ASimulationControl sc) {
		super(parent, style, ShowButtons.NONE);

		this.parent = parent;
		this.sc = sc;

		init();
	}

	/**
	 * Init the gui
	 */
	public abstract void init();

	@Override
	public abstract void save();

	@Override
	public abstract void reset();

}

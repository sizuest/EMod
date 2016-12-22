package ch.ethz.inspire.emod.gui.utils;

import ch.ethz.inspire.emod.ConfigurationChecker;
import ch.ethz.inspire.emod.simulation.ConfigState;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

/**
 * Graphical display of the configuration status of
 * - the machine set-up (M)
 * - the simulation configuration (S)
 * - the process settings (P)
 * @author simon
 *
 */
public class ConfigStatusGUI extends Composite {

	private Label labelMachine, labelSim, labelProc;

	/**
	 * @param parent
	 */
	public ConfigStatusGUI(Composite parent) {
		super(parent, SWT.NONE);

		this.setLayout(new GridLayout(3, true));

		labelMachine = new Label(this, SWT.BORDER);
		labelMachine.setText(" M ");

		labelSim = new Label(this, SWT.BORDER);
		labelSim.setText(" C ");

		labelProc = new Label(this, SWT.BORDER);
		labelProc.setText(" P ");

		this.layout();
		this.pack();

	}
	
	/**
	 * Update based on config checker
	 */
	public void updateStatus(){
		setMachineConfigState(ConfigurationChecker.checkMachineConfig().getStatus());
		//TODO S + P
	}

	/**
	 * Sets the status of the machine configuration
	 * @param state
	 */
	public void setMachineConfigState(ConfigState state) {
		setStatus(labelMachine, state);
	}

	/**
	 * Sets the status of the simulation configuration
	 * @param state
	 */
	public void setSimulationConfigState(ConfigState state) {
		setStatus(labelSim, state);
	}

	/**
	 * Sets the status of the process configuration
	 * @param state
	 */
	public void setProcessConfigState(ConfigState state) {
		setStatus(labelProc, state);
	}

	/**
	 * Sets the
	 * 
	 * @param label
	 * @param state
	 */
	public static void setStatus(Label label, ConfigState state) {
		switch (state) {
		case OK:
			label.setBackground(new Color(label.getDisplay(), 0, 255, 0));
			label.setForeground(new Color(label.getDisplay(), 0, 0, 0));
			break;
		case WARNING:
			label.setBackground(new Color(label.getDisplay(), 255, 255, 0));
			label.setForeground(new Color(label.getDisplay(), 0, 0, 0));
			break;
		case ERROR:
			label.setBackground(new Color(label.getDisplay(), 255, 0, 0));
			label.setForeground(new Color(label.getDisplay(), 255, 255, 255));
			break;

		}

	}

	/**
	 * Create Config status display in a new Window
	 * 
	 * @return
	 */
	public static ConfigStatusGUI newConfigStatusDisplay() {
		final Shell shell = new Shell(Display.getCurrent(), SWT.SYSTEM_MODAL
				| SWT.CLOSE);
		shell.setLocation(Display.getCurrent().getBounds().x / 2, Display
				.getCurrent().getBounds().y / 2);

		ConfigStatusGUI gui = new ConfigStatusGUI(shell);

		Display display = Display.getCurrent();
		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();

		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;

		shell.setLocation(x, y);
		// open the new shell
		shell.open();
		shell.pack();

		gui.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				shell.dispose();
			}
		});

		return gui;
	}

}

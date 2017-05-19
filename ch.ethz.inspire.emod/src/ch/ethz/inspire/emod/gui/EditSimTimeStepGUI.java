package ch.ethz.inspire.emod.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Spinner;

import ch.ethz.inspire.emod.EModSession;
import ch.ethz.inspire.emod.utils.ConfigReader;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

/**
 * Implements a GUI to configure the simulation time step
 * @author Simon Z�st
 *
 */
public class EditSimTimeStepGUI extends Composite{
	/* Labels */
	Label labelTimeStep;
	
	/* Text Fields */
	Spinner  spinnTimeStep;
	
	/* Sliders */
	Scale scaleTimeStep;
	
	
	EditSimTimeStepGUI(Composite parent, int style) {
		super(parent, style);
		
		this.setLayout(new GridLayout(3, false));
		
		labelTimeStep = new Label(this, SWT.NONE);
		labelTimeStep.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		labelTimeStep.setText(LocalizationHandler.getItem("app.gui.sim.integrator.timestep"));
		
		spinnTimeStep = new Spinner(this, SWT.BORDER);
		spinnTimeStep.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		spinnTimeStep.setMinimum(1);
		spinnTimeStep.setIncrement(1);
		spinnTimeStep.setSelection(1);
		
		scaleTimeStep = new Scale(this, SWT.NONE);
		scaleTimeStep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scaleTimeStep.setMinimum(1);
		scaleTimeStep.setMaximum(10);
		scaleTimeStep.setSelection(1);
		scaleTimeStep.setIncrement(1);
		scaleTimeStep.setBackground(parent.getBackground());
		
		coupleInputs(spinnTimeStep, scaleTimeStep);
		
		loadFromSimConfigFile();
		
		this.layout();
	}
	
	/**
	 * Returns the time step
	 * @return
	 */
	public int getTimestep(){
		return spinnTimeStep.getSelection();
	}
	
	/**
	 * Sets the time step
	 * @param value
	 */
	public void setTimestep(int value){
		spinnTimeStep.setSelection(value);
		syncInputs(spinnTimeStep, scaleTimeStep, true);
	}
	
	/**
	 * Sync the values of the spinner and the scale
	 * @param spinn
	 * @param scale
	 * @param fromSpinner2Scale
	 */
	private void syncInputs(Spinner spinn, Scale scale, boolean fromSpinner2Scale){
		if(fromSpinner2Scale)
			scale.setSelection(spinn.getSelection());
		else
			spinn.setSelection(scale.getSelection());
	}
	
	/**
	 * Loads and displays the data from the simulation config
	 */
	public void loadFromSimConfigFile(){
		try {
			ConfigReader simulationConfigReader = new ConfigReader(EModSession.getSimulationConfigPath());
			simulationConfigReader.ConfigReaderOpen();
			
			setTimestep(simulationConfigReader.getValue("simulationPeriod", 1));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves the configured values to the sim config file
	 */
	public void saveToSimConfigFile(){
		try {
			ConfigReader simulationConfigReader = new ConfigReader(EModSession.getSimulationConfigPath());
			simulationConfigReader.ConfigReaderOpen();
			
			simulationConfigReader.setValue("simulationPeriod", getTimestep());

			simulationConfigReader.saveValues();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void update(){
		loadFromSimConfigFile();
		this.layout();
	}
	
	/**
	 * Connects a text field and a slider
	 * @param text
	 * @param scale
	 */
	private void coupleInputs(final Spinner spinn, final Scale scale){
		
		/* Scale -> Spinner */
		scale.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				syncInputs(spinn, scale, false);
				saveToSimConfigFile();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		/* Spinner -> Scale */
		spinn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				syncInputs(spinn, scale, true);
				saveToSimConfigFile();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
	}
	
	

}

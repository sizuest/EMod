package ch.ethz.inspire.emod.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;

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
	Text  textTimeStep;
	
	/* Sliders */
	Scale scaleTimeStep;
	
	
	EditSimTimeStepGUI(Composite parent, int style) {
		super(parent, style);
		
		this.setLayout(new GridLayout(3, false));
		
		labelTimeStep = new Label(this, SWT.NONE);
		labelTimeStep.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		labelTimeStep.setText(LocalizationHandler.getItem("app.gui.sim.integrator.timestep"));
		
		textTimeStep = new Text(this, SWT.BORDER);
		textTimeStep.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		//textTimeStep.setMinimum(1);
		//textTimeStep.setIncrement(1);
		//textTimeStep.setSelection(1);
		textTimeStep.setText("10.0");
		
		scaleTimeStep = new Scale(this, SWT.NONE);
		scaleTimeStep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scaleTimeStep.setMinimum(1);
		scaleTimeStep.setMaximum(50);
		scaleTimeStep.setSelection(1);
		scaleTimeStep.setIncrement(1);
		scaleTimeStep.setBackground(parent.getBackground());
		
		coupleInputs(textTimeStep, scaleTimeStep);
		
		this.layout();
		
		loadFromSimConfigFile();
	}
	
	/**
	 * Returns the time step
	 * @return
	 */
	public double getTimestep(){
		double cand = Double.valueOf(textTimeStep.getText());
		if(Double.isNaN(cand) | cand<=0){
			loadFromSimConfigFile();
			return getTimestep();
		}
		
		return cand;
	}
	
	/**
	 * Sets the time step
	 * @param value
	 */
	public void setTimestep(double value){
		textTimeStep.setText(""+value);
		syncInputs(textTimeStep, scaleTimeStep, true);
	}
	
	/**
	 * Sync the values of the spinner and the scale
	 * @param text
	 * @param scale
	 * @param fromText2Scale
	 */
	private void syncInputs(Text text, Scale scale, boolean fromText2Scale){
		if(fromText2Scale)
			scale.setSelection((int) (getTimestep()*10));
		else
			text.setText(""+scale.getSelection()/10.0);
	}
	
	/**
	 * Loads and displays the data from the simulation config
	 */
	public void loadFromSimConfigFile(){
		try {
			ConfigReader simulationConfigReader = new ConfigReader(EModSession.getSimulationConfigPath());
			simulationConfigReader.ConfigReaderOpen();
			
			setTimestep(simulationConfigReader.getValue("simulationPeriod", 1.0));
			
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
	private void coupleInputs(final Text text, final Scale scale){
		
		/* Scale -> Spinner */
		scale.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				syncInputs(text, scale, false);
				saveToSimConfigFile();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		/* Spinner -> Scale */
		text.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				syncInputs(text, scale, true);
				saveToSimConfigFile();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
	}
	
	

}

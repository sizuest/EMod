package ch.ethz.inspire.emod.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.EModSession;
import ch.ethz.inspire.emod.utils.ConfigReader;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

/**
 * Implements the SWT interface to edit the parameters of 
 * a fluid circuit solver
 * 
 * @author Simon Z�st
 *
 */
public class EditFluidSolverParametersGUI extends Composite{
	
	/* Labels */
	Label labelMaxIter;
	Label labelTolE;
	Label labelMinFlowRate;
	
	/* Text Fields */
	Spinner  spinnMaxIter;
	Text     spinnTolE;
	Text     spinnMinFlowRate;
	
	/* Sliders */
	Scale scaleMaxIter;
	Scale scaleTolE;
	Scale scaleMinFlowRate;
	
	
	/* Values */
	static int minExpTolE = -9;
	static int maxExpTolE = -1;
	static int minMinFlowRate = -12;
	static int maxMinFlowRate = -3;
	
	

	/**
	 * @param parent
	 * @param style
	 */
	public EditFluidSolverParametersGUI(Composite parent, int style) {
		super(parent, style);

		this.setLayout(new GridLayout(3, false));
		
		labelMaxIter = new Label(this, SWT.NONE);
		labelMaxIter.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		labelMaxIter.setText(LocalizationHandler.getItem("app.gui.sim.fc.maxiter"));
		
		spinnMaxIter = new Spinner(this, SWT.BORDER);
		spinnMaxIter.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		spinnMaxIter.setMinimum(1);
		spinnMaxIter.setIncrement(20);
		spinnMaxIter.setSelection(50);
		
		scaleMaxIter = new Scale(this, SWT.NONE);
		scaleMaxIter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scaleMaxIter.setMinimum(1);
		scaleMaxIter.setMaximum(100);
		scaleMaxIter.setSelection(20);
		scaleMaxIter.setIncrement(5);
		
		labelTolE = new Label(this, SWT.NONE);
		labelTolE.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		labelTolE.setText(LocalizationHandler.getItem("app.gui.sim.fc.reltol"));
		
		spinnTolE = new Text(this, SWT.BORDER);
		spinnTolE.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		spinnTolE.setText(""+1E-4);
		
		scaleTolE = new Scale(this, SWT.NONE);
		scaleTolE.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scaleTolE.setMinimum(0);
		scaleTolE.setMaximum(maxExpTolE-minExpTolE);
		scaleTolE.setSelection(1);
		scaleTolE.setIncrement(1);
		
		labelMinFlowRate = new Label(this, SWT.NONE);
		labelMinFlowRate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		labelMinFlowRate.setText(LocalizationHandler.getItem("app.gui.sim.fc.minflowrate"));
		
		spinnMinFlowRate = new Text(this, SWT.BORDER);
		spinnMinFlowRate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		spinnMinFlowRate.setText(""+1E-9);
		
		scaleMinFlowRate = new Scale(this, SWT.NONE);
		scaleMinFlowRate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scaleMinFlowRate.setMinimum(0);
		scaleMinFlowRate.setMaximum(maxMinFlowRate-minMinFlowRate);
		scaleMinFlowRate.setSelection(1);
		scaleMinFlowRate.setIncrement(1);
		
		coupleInputs(spinnMaxIter, scaleMaxIter);
		coupleInputs(spinnMinFlowRate, scaleMinFlowRate, minMinFlowRate, maxMinFlowRate);
		coupleInputs(spinnTolE, scaleTolE, minExpTolE, maxExpTolE);
		
		loadFromSimConfigFile();
		
		this.pack();
		
	}
	
	@Override
	public void update(){
		loadFromSimConfigFile();
		this.layout();
	}
	
	
	/**
	 * Loads and displays the data from the simulation config
	 */
	public void loadFromSimConfigFile(){
		try {
			ConfigReader simulationConfigReader = new ConfigReader(EModSession.getSimulationConfigPath());
			simulationConfigReader.ConfigReaderOpen();
			
			setMaxIterations(simulationConfigReader.getValue("FluidSolver.MaxIter", 20));
			setRelativeTolerance(simulationConfigReader.getValue("FluidSolver.RelTol", 1E-4));
			setMinFlowRate(simulationConfigReader.getValue("FluidSolver.MinFlowRate", 1E-9));
			
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
			
			simulationConfigReader.setValue("FluidSolver.MaxIter", getMaxIterations());
			simulationConfigReader.setValue("FluidSolver.RelTol", getRelativeTolerance());
			simulationConfigReader.setValue("FluidSolver.MinFlowRate", getMinFlowRate());

			simulationConfigReader.saveValues();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the configurated interation limit
	 * @return
	 */
	public int getMaxIterations(){
		return spinnMaxIter.getSelection();
	}
	
	/**
	 * Returns the configures tolerance limit for relative changes
	 * @return
	 */
	public double getRelativeTolerance(){
		return Double.valueOf(spinnTolE.getText());
	}
	
	/**
	 * Returns the configured limit for flow rates
	 * @return
	 */
	public double getMinFlowRate(){
		return Double.valueOf(spinnMinFlowRate.getText());
	}
	
	/**
	 * Sets the number of interations
	 * @param value
	 */
	public void setMaxIterations(int value){
		spinnMaxIter.setSelection(value);
		syncInputs(spinnMaxIter, scaleMaxIter, true);
	}
	
	/**
	 * Sets the relative tolerance
	 * @param value
	 */
	public void setRelativeTolerance(double value){
		spinnTolE.setText(""+value);
		syncInputs(spinnTolE, scaleTolE, true, minExpTolE, maxExpTolE);
	}
	
	/**
	 * sets the min flow rate
	 * @param value
	 */
	public void setMinFlowRate(double value){
		spinnMinFlowRate.setText(""+value);
		syncInputs(spinnMinFlowRate, scaleMinFlowRate, true, minMinFlowRate, maxMinFlowRate);
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
	 * Sync the values of the text field and the scale
	 * @param text
	 * @param scale
	 * @param fromSpinner2Scale
	 * @param min
	 * @param max
	 */
	private void syncInputs(Text text, Scale scale, boolean fromSpinner2Scale, double min, double max){
		if(fromSpinner2Scale)
			// Apply log and lin scaling
			scale.setSelection( (int) ((Math.log10(Double.valueOf(text.getText()))-min)/(max-min) * (scale.getMaximum()-scale.getMinimum()) + scale.getMinimum()));
		else
			// Apply log and lin scaling
			text.setText(""+ (Math.pow(10, ((double) scale.getSelection()-scale.getMinimum())/(scale.getMaximum()-scale.getMinimum()) * (max-min) + min)));
		
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
	
	/**
	 * Connects a text field and a slider
	 * @param text
	 * @param scale
	 */
	private void coupleInputs(final Text text, final Scale scale, final int min, final int max){
		
		/* Scale -> Text */
		scale.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				syncInputs(text, scale, false, min, max);
				saveToSimConfigFile();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		/* Text -> Scale */
		text.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					Double.valueOf(text.getText());
					syncInputs(text, scale, true, min, max);
					saveToSimConfigFile();
				} catch(NumberFormatException ee){}				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {}
		});
		
		text.addFocusListener(new FocusListener() {
			
			double lastValue = 0;
			
			@Override
			public void focusLost(FocusEvent e) {
				double value;
				try {
					value = Double.valueOf(text.getText());
					syncInputs(text, scale, true, min, max);
					saveToSimConfigFile();
				} catch(NumberFormatException ee){
					value = lastValue;
					text.setText(""+value);
				}
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				lastValue = Double.valueOf(text.getText());
			}
		});
		
		
		
	}

}

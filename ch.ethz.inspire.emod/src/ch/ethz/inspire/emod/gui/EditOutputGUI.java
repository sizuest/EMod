package ch.ethz.inspire.emod.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.ethz.inspire.emod.EModSession;
import ch.ethz.inspire.emod.utils.ConfigReader;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

/**
 * Implements a GUI to control the simulation output
 * @author Simon Züst
 *
 */
public class EditOutputGUI extends Composite{
	
	protected Button buttonFEM;

	/**
	 * @param parent
	 * @param style
	 */
	public EditOutputGUI(Composite parent, int style) {
		super(parent, style);

		this.setLayout(new GridLayout(2, false));
		
		Label labelFEM;
		
		labelFEM = new Label(this, SWT.NONE);
		labelFEM.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		labelFEM.setText(LocalizationHandler.getItem("app.gui.sim.output.fem"));
		
		buttonFEM = new Button(this, SWT.CHECK);
		buttonFEM.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		buttonFEM.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				saveToSimConfigFile();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

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
			
			setFEMOutput(simulationConfigReader.getValue("Output.FEM", true));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setFEMOutput(Boolean value) {
		buttonFEM.setSelection(value);
	}
	
	private Object getFEMOuput() {
		return buttonFEM.getSelection();
	}


	/**
	 * Saves the configured values to the sim config file
	 */
	public void saveToSimConfigFile(){
		try {
			ConfigReader simulationConfigReader = new ConfigReader(EModSession.getSimulationConfigPath());
			simulationConfigReader.ConfigReaderOpen();
			
			simulationConfigReader.setValue("Output.FEM", getFEMOuput());

			simulationConfigReader.saveValues();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



}

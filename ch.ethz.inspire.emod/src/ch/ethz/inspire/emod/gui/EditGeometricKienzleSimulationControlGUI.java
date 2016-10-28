package ch.ethz.inspire.emod.gui;

import java.io.IOException;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.gui.utils.TableUtils;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.simulation.ASimulationControl;
import ch.ethz.inspire.emod.utils.ConfigReader;
import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

public class EditGeometricKienzleSimulationControlGUI extends AEditInputComposite {
	
	private Table tableInputProperties;
	protected ConfigReader input;
	
	public EditGeometricKienzleSimulationControlGUI(Composite parent, int style, ASimulationControl sc) {
		super(parent, style, sc);

	}


	@Override
	public void init() {
		
		this.getContent().setLayout(new GridLayout(1, true));
		
		tableInputProperties = new Table(this.getContent(), SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
    	tableInputProperties.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    	tableInputProperties.setLinesVisible(true);
    	tableInputProperties.setHeaderVisible(true);
    	
    	String[] titles =  {LocalizationHandler.getItem("app.gui.compdb.property"),
    						LocalizationHandler.getItem("app.gui.compdb.value") };
		for(int i=0; i < titles.length; i++){
			TableColumn column = new TableColumn(tableInputProperties, SWT.NULL);
			column.setText(titles[i]);
		}
		
		try {
			TableUtils.addCellEditor(tableInputProperties, this, new int[]{1});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String path = PropertiesHandler.getProperty("app.MachineDataPathPrefix") + "/" +
				  PropertiesHandler.getProperty("sim.MachineName") + "/" +
				  "MachineConfig/" +
				  PropertiesHandler.getProperty("sim.MachineConfigName") + "/" +
				  sc.getType() + "_" + sc.getName() + ".xml";
  	
	  	try {
			input = new ConfigReader(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	  	
	  	update();
	}
	
	@Override
    public void update(){
    	tableInputProperties.setItemCount(0);	
    	
    	String[] keysInputs = new String[]{"ap_name", "n_name", "v_name", "d_name"};
    	String[] keysValues = new String[]{"kappa", "kc", "z"};
    	
    	/*
    	 * Obtain the available simulation controls and put their names into 
    	 */
    	List<ASimulationControl> scListMeter    = Machine.getProcessSimulationControlList(new SiUnit("m")),
    			                 scListRotSpeed = Machine.getProcessSimulationControlList(new SiUnit("Hz")),
    			                 scListLinSpeed = Machine.getProcessSimulationControlList(new SiUnit("m/s"));
    	
    	String[] scNameMeter    = new String[scListMeter.size()],
    			 scNameRotSpeed = new String[scListRotSpeed.size()],
    			 scNameLinSpeed = new String[scListLinSpeed.size()];
    	
    	for(int i=0; i<scNameMeter.length; i++)
    		scNameMeter[i] = scListMeter.get(i).getName();
    	
    	for(int i=0; i<scNameRotSpeed.length; i++)
    		scNameRotSpeed[i] = scListRotSpeed.get(i).getName();
    	
    	for(int i=0; i<scNameLinSpeed.length; i++)
    		scNameLinSpeed[i] = scListLinSpeed.get(i).getName();
    	
    	
    	/*
    	 * Add the input combos to let the user select the process signal required
    	 */
    	for(String key: keysInputs){
			final TableItem item = new TableItem(tableInputProperties, SWT.NONE);
			item.setText(0, key);
			
			TableEditor editor = new TableEditor(tableInputProperties);
	        final CCombo comboInput = new CCombo(tableInputProperties, SWT.PUSH);
	        
	        // Set list of simulation controls with appropriate unit
	        switch(key){
	        case "ap_name":
	        case "d_name":
	        	comboInput.setItems(scNameMeter);
	        	break;
	        case "n_name":
	        	comboInput.setItems(scNameRotSpeed);
	        	break;
	        case "v_name":
	        	comboInput.setItems(scNameLinSpeed);
	        }
	        
			
			try {
				item.setText(1, input.getString(key));
				comboInput.setText(input.getString(key));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			comboInput.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					item.setText(1, comboInput.getText());
					wasEdited();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {/* Not used */}
			});
			
			editor.grabHorizontal = true;
	        editor.horizontalAlignment = SWT.LEFT;
			editor.setEditor(comboInput, item, 1);
		}
	
		for(String key: keysValues){
			TableItem item = new TableItem(tableInputProperties, SWT.NONE);
			item.setText(0, key);
			try {
				item.setText(1, input.getString(key));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		TableColumn[] columns = tableInputProperties.getColumns();
        for (int j = 0; j < columns.length; j++) {
          columns[j].pack();
        }
    	
    }

	@Override
	public void save() {
		for(TableItem ti: tableInputProperties.getItems())
			input.setValue(ti.getText(0), ti.getText(1));
		
		try {
			input.saveValues();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		sc.readConfig();
		
	}

	@Override
	public void reset() {
		try {
			input = new ConfigReader(input.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		update();
	}

}

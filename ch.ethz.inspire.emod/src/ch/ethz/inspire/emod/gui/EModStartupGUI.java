package ch.ethz.inspire.emod.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

public class EModStartupGUI {
	private static Shell shell;
	
	public EModStartupGUI(){
    shell = new Shell(Display.getCurrent(),SWT.APPLICATION_MODAL);
    
	//TODO manick: startup window

	shell.setText("EMod startup");
	shell.setLayout(new GridLayout(2, true));
	
	String machineName = PropertiesHandler.getProperty("sim.MachineName");
	String machineConfigName = PropertiesHandler.getProperty("sim.MachineConfigName");
	
	System.out.println("Machineconfig and simconfig from last use loaded");
	
	Text aText = new Text(shell, SWT.LEFT);
	GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
	gridData.horizontalSpan = 1;
	aText.setLayoutData(gridData);
	aText.setText(LocalizationHandler.getItem("app.gui.startup.machconfig"));
	
	Combo comboMachineName = new Combo(shell, SWT.NONE);
	gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
	gridData.horizontalSpan = 1;
	comboMachineName.setLayoutData(gridData);
	comboMachineName.setText(machineName);
	
	Text bText = new Text(shell, SWT.LEFT);
	gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
	gridData.horizontalSpan = 1;
	bText.setLayoutData(gridData);
	bText.setText(LocalizationHandler.getItem("app.gui.startup.simconfig"));
	
	Combo comboConfigName = new Combo(shell, SWT.NONE);
	gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
	gridData.horizontalSpan = 1;
	comboConfigName.setLayoutData(gridData);
	comboConfigName.setText(machineConfigName);
	
	Button buttonContinue = new Button(shell, SWT.NONE);
	gridData = new GridData(GridData.END, GridData.CENTER, true, false);
	gridData.horizontalSpan = 2;
	buttonContinue.setLayoutData(gridData);
	buttonContinue.setText(LocalizationHandler.getItem("app.gui.continue"));
    buttonContinue.addSelectionListener(new SelectionListener(){
    	public void widgetSelected(SelectionEvent event){
    		shell.close();
    		System.out.println("Machine config and sim config loaded");
    	}
    	public void widgetDefaultSelected(SelectionEvent event){
    		
    	}
    });
	
	
	shell.pack();

	Rectangle rect = shell.getBounds();
	
	//width and height of the shell
	int[] size = {0, 0};
	size[0] = rect.width;
	size[1] = rect.height;
	
	//position the shell into the middle of the last window
    int[] position;
    position = EModGUI.shellPosition();
    shell.setLocation(position[0]-size[0]/2, position[1]-size[1]/2);
	
    //open the new shell
	shell.open();
	}
	
	
	
    public void closeLinkingGUI(){
    	shell.close();
    }
}

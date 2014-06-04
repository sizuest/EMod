package ch.ethz.inspire.emod.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;
import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

public class ComponentEditGUI {

    private Shell shell;

    public ComponentEditGUI(){
    	
	    }

	public void openComponentEditGUI(MachineComponent mc){
        	shell = new Shell(Display.getCurrent());
		
	        System.out.println("LinkingGUI opened");
	        
	        shell.setText(LocalizationHandler.getItem("app.gui.linking.title"));

	    	shell.setLayout(new GridLayout(2, false));
	    	
	    	Text textTitle = new Text(shell, SWT.NONE);
			textTitle.setText(LocalizationHandler.getItem("app.gui.model.editcomp"));
			GridData gridData = new GridData(GridData.BEGINNING, GridData.CENTER, true, true);
			gridData.horizontalSpan = 2;
			textTitle.setLayoutData(gridData);
			
			Text textCompName = new Text(shell, SWT.NONE);
			textCompName.setText(LocalizationHandler.getItem("app.gui.model.name") + ":");
			gridData = new GridData(GridData.BEGINNING, GridData.CENTER, true, true);
			gridData.horizontalSpan = 1;
			textCompName.setLayoutData(gridData);;
			
			Text textCompNameValue = new Text(shell, SWT.NONE);
			textCompNameValue.setText(mc.getName());
			textCompNameValue.setLayoutData(gridData);
			
			Text textCompType = new Text(shell, SWT.NONE);
			textCompType.setText(LocalizationHandler.getItem("app.gui.model.type") + ":");
			textCompType.setLayoutData(gridData);
			
			Text textCompTypeValue = new Text(shell, SWT.NONE);
			textCompTypeValue.setText(mc.getComponent().getClass().toString().replace("class ch.ethz.inspire.emod.model.",""));
			textCompTypeValue.setLayoutData(gridData);
			
			Text textCompParam = new Text(shell, SWT.NONE);
			textCompParam.setText(LocalizationHandler.getItem("app.gui.model.param") + ":");
			textCompParam.setLayoutData(gridData);
			
			Text textCompParamValue = new Text(shell, SWT.NONE);
			textCompParamValue.setText(mc.getComponent().getType());
			textCompParamValue.setLayoutData(gridData);
			
			
			/*/
			//TODO manick: show xml content??
			JAXBContext context = JAXBContext.newInstance(mc.getClass());
			Unmarshaller um = context.createUnmarshaller();
			
			String path = PropertiesHandler.getProperty("app.MachineComponentDBPathPrefix") + "/Motor/Motor_siemens123.xml";
			
			File file = new File(path);
			
			String string = (String) um.unmarshal(new FileReader(file));
			
			Text aText = new Text(shell, SWT.NONE);
			aText.setText(string);
			textCompParam.setLayoutData(gridData);
			//*/
			
			
			/*/
			//TODO manick: show parameters of component
			ComponentConfigReader params = null;
			// Open file containing the parameters of the model type:
			try {
				params = new ComponentConfigReader("Motor", mc.getComponent().getType());
			}
			catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}

			
			Text textXMLConfig = new Text(shell, SWT.NONE);
			textXMLConfig.setText(params.toString());
			textXMLConfig.setLayoutData(gridData);

			//*/
			
			
			
			Button buttonSave = new Button(shell, SWT.NONE);
			buttonSave.setText(LocalizationHandler.getItem("app.gui.save"));
			buttonSave.addSelectionListener(new SelectionListener(){
		    	public void widgetSelected(SelectionEvent event){
		    		closeComponentEditGUI();

		    		System.out.println("Button Save Component Edit GUI");
		    	}
		    	public void widgetDefaultSelected(SelectionEvent event){
		    		
		    	}
		    });
			gridData = new GridData(GridData.END, GridData.CENTER, true, false);
			gridData.horizontalSpan = 2;
			buttonSave.setLayoutData(gridData);
			
			
			shell.pack();

			//width and height of the shell
			Rectangle rect = shell.getBounds();
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
	
	

    public void closeComponentEditGUI(){
    	shell.close();
    }
}

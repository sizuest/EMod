package ch.ethz.inspire.emod.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;

import ch.ethz.inspire.emod.gui.utils.MachineComponentHandler;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

/**
 * Machine component selection GUI
 * @author sizuest
 *
 */
public class SelectMachineComponentGUI {
	private Shell shell;
	
	//tree to list all the components
	private Tree treeComponentDBView;
	
	/**
	 * SelectMachineComponentGUI
	 */
	public SelectMachineComponentGUI(){
		shell = new Shell(Display.getCurrent());
		shell.setText(LocalizationHandler.getItem("app.gui.compdb.editcomp"));
		shell.setSize(400, 600);
		shell.setLayout(new GridLayout(2, false));
	}
	
	private void init(String type){
		//create tree element and fill it with the components from the DB
		treeComponentDBView = new Tree(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		treeComponentDBView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		MachineComponentHandler.fillMachineComponentTree(type, treeComponentDBView);
	}
	
	/**
	 * @param type 
	 * @param item
	 * @param index
	 */
	public void getSelectionToTable(String type, final TableItem item, final int index){

		/* New GUI */
		init(type);		
		
		/* Add Button */
		final Button addComponentButton = new Button(shell, SWT.PUSH);
		addComponentButton.setText("Add");
		addComponentButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		addComponentButton.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){
        		if(!(getSelectionToString().matches("")))
        			if(item.getText().matches(""))
        				item.setText(index, getSelectionToString());
        			else
        				item.setText(index, item.getText(index)+", "+getSelectionToString());
        	}
        	public void widgetDefaultSelected(SelectionEvent event){
        		
        	}
        });
		addComponentButton.pack();
		
		/* Select Button */
		final Button selectComponentButton = new Button(shell, SWT.PUSH);
		selectComponentButton.setText("Select");
		selectComponentButton.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		selectComponentButton.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){     		
        		item.setText(index, getSelectionToString());
        		shell.close();
        	}
        	public void widgetDefaultSelected(SelectionEvent event){
        		
        	}
        });
		selectComponentButton.pack();
		

		shell.open();
		
	}
	
	private String getSelectionToString(){
		int n = treeComponentDBView.getSelectionCount();
		String out = "";
		
		for(int i=0; i<n; i++){
			if(i+1==n)
				out+=treeComponentDBView.getSelection()[i].getText();
			else
				out+=treeComponentDBView.getSelection()[i].getText()+", ";
		}
		
		return out;
	}
}

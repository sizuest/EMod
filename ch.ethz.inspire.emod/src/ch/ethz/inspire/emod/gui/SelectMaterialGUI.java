package ch.ethz.inspire.emod.gui;

import java.lang.reflect.Method;

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

import ch.ethz.inspire.emod.gui.utils.MaterialHandler;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

/**
 * Implements a Material selection GUI;
 * @author sizuest
 *
 */
public class SelectMaterialGUI {
	private Shell shell;
	
	//tree to list all the materials
	private Tree treeMaterialDBView;
	
	
	/**
	 * SelectMaterialGUI
	 */
	public SelectMaterialGUI(){
		shell = new Shell(Display.getCurrent());
		shell.setText(LocalizationHandler.getItem("app.gui.matdb.title"));
		shell.setSize(400, 600);
		shell.setLayout(new GridLayout(2, false));
	}
	
	private void init(){
		//create tree element and fill it with the components from the DB
		treeMaterialDBView = new Tree(shell, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		treeMaterialDBView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		MaterialHandler.fillTree(treeMaterialDBView);
	}
	
	/**
	 * @param fun 
	 * @param funObj 
	 * @param item
	 * @param index
	 */
	public void getSelectionToTable(final Method fun, final Object funObj){

		/* New GUI */
		init();
		
		
		final Button selectMaterialButton = new Button(shell, SWT.PUSH);
		selectMaterialButton.setText("Select");
		selectMaterialButton.pack();
		selectMaterialButton.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1));
		selectMaterialButton.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){     
        		try {
    				fun.invoke(funObj, treeMaterialDBView.getSelection()[0].getText());
    			} catch (Throwable e) {
    				e.printStackTrace();
    			}
        		shell.close();
        	}
        	public void widgetDefaultSelected(SelectionEvent event){
        		
        	}
        });
		
		shell.open();
		
		
	}
	
	
	
}

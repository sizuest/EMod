package ch.ethz.inspire.emod.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import ch.ethz.inspire.emod.gui.utils.MaterialHandler;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

public class MaterialDBGUI {

	private Shell shell;
	
	//tree to list all the components
	private Tree treeComponentDBView;

	/**
	 * window with the component db to select a component to edit
	 */ 	
	public MaterialDBGUI(){
		shell = new Shell(Display.getCurrent());
		shell.setText(LocalizationHandler.getItem("app.gui.matdb.title"));
		shell.setSize(400, 600);
		shell.setLayout(new GridLayout(2, false));
	
		//create tree element and fill it with the components from the DB
		treeComponentDBView = new Tree(shell, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		treeComponentDBView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		MaterialHandler.fillTree(treeComponentDBView);

		//show button to edit the selected component
		Button buttonEdit = new Button(shell, SWT.NONE);
		buttonEdit.setText(LocalizationHandler.getItem("app.gui.matdb.editmat"));
		buttonEdit.setLayoutData(new GridData(SWT.FILL, SWT.END, true, false, 1, 1));
		//selection listener for the button
		buttonEdit.addSelectionListener(new SelectionListener(){
			//when selected -> get selection -> get file of component -> open window to edit said component
			public void widgetSelected(SelectionEvent event){
				//get the selection of the tree and set it as event data
				TreeItem[] selection = null;
				selection = treeComponentDBView.getSelection();
				String text = "";
				for(TreeItem item:selection){
					text += (String)item.getText();	
				}
				event.data = text;
	    		
				//split the given string into component name and type
				String[] split = text.split("_",2);
				split[1] = split[1].replace(".xml", "");
				
				//open window editComponentEditGUI with the selected component
				EditMaterialGUI materialEditGUI = new EditMaterialGUI();
				materialEditGUI.editMaterialGUI(split[0], split[1]);
	    	}
	    	public void widgetDefaultSelected(SelectionEvent event){
	    		
	    	}
	    });
		
		//button to close the window
		Button buttonClose = new Button(shell, SWT.NONE);
		buttonClose.setText(LocalizationHandler.getItem("app.gui.close"));
		buttonClose.setLayoutData(new GridData(SWT.FILL, SWT.END, true, false, 1, 1));
		buttonClose.addSelectionListener(new SelectionListener(){
	    	public void widgetSelected(SelectionEvent event){
	    		//no special actions need to be done
	    		shell.close();
	    	}
	    	public void widgetDefaultSelected(SelectionEvent event){
	    		
	    	}
	    });
			
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
}
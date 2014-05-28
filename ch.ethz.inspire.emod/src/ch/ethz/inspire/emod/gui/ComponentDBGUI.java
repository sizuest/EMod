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

import ch.ethz.inspire.emod.gui.utils.ComponentHandler;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

public class ComponentDBGUI {

	private Shell shell;
	private Tree treeComponentDBView;
	private Button buttonEdit;
	private Button buttonClose;
	
	public ComponentDBGUI(){
		shell = new Shell(Display.getCurrent());
		shell.setText(LocalizationHandler.getItem("app.gui.compdb.title"));
		shell.setSize(400, 600);
		
		shell.setLayout(new GridLayout(2, false));
	
		treeComponentDBView = new Tree(shell, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.horizontalSpan = 2;
		treeComponentDBView.setLayoutData(gridData);
		
		//Tree füllen mit aktuellen Werten aus dem Verzeichnis der DB
		ComponentHandler.fillTree(treeComponentDBView);

		buttonEdit = new Button(shell, SWT.BORDER);
		buttonEdit.setText(LocalizationHandler.getItem("app.gui.compdb.editcomp"));
		gridData = new GridData(GridData.FILL, GridData.VERTICAL_ALIGN_END, true, false);
		gridData.horizontalSpan = 1;
		buttonEdit.setLayoutData(gridData);
		buttonEdit.addSelectionListener(new SelectionListener(){
	    	public void widgetSelected(SelectionEvent event){
	    		shell.close();
	    		System.out.println("Edit Component");
	    	}
	    	public void widgetDefaultSelected(SelectionEvent event){
	    		
	    	}
	    });
		
		buttonClose = new Button(shell, SWT.BORDER);
		buttonClose.setText(LocalizationHandler.getItem("app.gui.close"));
		gridData = new GridData(GridData.FILL, GridData.VERTICAL_ALIGN_END, true, false);
		gridData.horizontalSpan = 1;
		buttonClose.setLayoutData(gridData);
		buttonClose.addSelectionListener(new SelectionListener(){
	    	public void widgetSelected(SelectionEvent event){
	    		shell.close();
	    		System.out.println("Edit Component");
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

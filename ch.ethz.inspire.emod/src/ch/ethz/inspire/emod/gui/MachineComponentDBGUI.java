package ch.ethz.inspire.emod.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
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

import ch.ethz.inspire.emod.gui.utils.MachineComponentHandler;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

/**
 * General machine component DB GUI
 * @author sizuest
 *
 */
public class MachineComponentDBGUI {

	private Shell shell;
	
	//tree to list all the components
	private Tree treeComponentDBView;

	/**
	 * window with the component db to select a component to edit
	 */ 	
	public MachineComponentDBGUI(){
		shell = new Shell(Display.getCurrent());
		shell.setText(LocalizationHandler.getItem("app.gui.compdb.title"));
		shell.setSize(400, 600);
		shell.setLayout(new GridLayout(2, false));
	
		//create ne tree element and fill it with the components from the DB
		treeComponentDBView = new Tree(shell, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		treeComponentDBView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		MachineComponentHandler.fillMachineComponentTree(treeComponentDBView);
		
		treeComponentDBView.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {/* mot used */}
			
			@Override
			public void mouseDown(MouseEvent e) {/* mot used */}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				TreeItem[] selection = null;
				selection = treeComponentDBView.getSelection();
				//open window editComponentEditGUI with the selected component
				EditMachineComponentGUI.editMachineComponentGUI(shell, selection[0].getParentItem().getText(), selection[0].getText());
			}
		});

		//show button to edit the selected component
		Button buttonEdit = new Button(shell, SWT.NONE);
		buttonEdit.setText(LocalizationHandler.getItem("app.gui.compdb.editcomp"));
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
	    	
				
				//open window editComponentEditGUI with the selected component
				EditMachineComponentGUI.editMachineComponentGUI(shell, selection[0].getParentItem().getText(), text);
	    	}
	    	public void widgetDefaultSelected(SelectionEvent event){
	    		// Not used
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
	    		// Not used
	    	}
	    });
			
		//width and height of the shell
		Rectangle rect = shell.getBounds();
		int[] size = {0, 0};
		size[0] = rect.width;
		size[1] = rect.height;
		
		//position the shell into the middle of the last window
	    //int[] position;
	    //position = EModGUI.shellPosition();
	    //shell.setLocation(position[0]-size[0]/2, position[1]-size[1]/2);
		
	    //open the new shell
		shell.open();
	    }
}

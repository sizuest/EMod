package ch.ethz.inspire.emod.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import ch.ethz.inspire.emod.gui.utils.MachineComponentHandler;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

/**
 * Machine component selection GUI
 * @author sizuest
 *
 */
public class SelectMachineComponentGUI extends Dialog{
	private String filter = "";
	private String input;
	
	//tree to list all the components
	private Tree treeComponentDBView;
	
	/**
	 * SelectMachineComponentGUI
	 * @param parent 
	 */
	public SelectMachineComponentGUI(Shell parent){
		this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	}
	
	public SelectMachineComponentGUI(Shell parent, int style) {
		super(parent, style);
	}
	
	public String open(){
		return open("");
	}
	
	public String open(String filter) {
		Shell shell = new Shell(getParent(), getStyle());
		shell.setText(LocalizationHandler.getItem("app.gui.compdb.editcomp"));
		shell.setSize(400, 600);
		shell.setLayout(new GridLayout(2, false));
		
		this.filter = filter;
		
		createContents(shell);
		shell.pack();
		shell.layout();
		shell.open();
		
		shell.setSize(400, 600);
		
		Display display = getParent().getDisplay();
	    while (!shell.isDisposed()) {
	      if (!display.readAndDispatch()) {
	        display.sleep();
	      }
	    }
	    // Return the entered value, or null
	    return input;
		
	}

	private void createContents(final Shell shell){		
		//create tree element and fill it with the components from the DB
		treeComponentDBView = new Tree(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		treeComponentDBView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		MachineComponentHandler.fillMachineComponentTree(filter, treeComponentDBView);
				
		/* Select Button */
		final Button selectComponentButton = new Button(shell, SWT.PUSH);
		selectComponentButton.setText("OK");
		selectComponentButton.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		selectComponentButton.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){     		
        		input = getSelectionToString();
        		shell.close();
        	}
        	public void widgetDefaultSelected(SelectionEvent event){
        		// Not used
        	}
        });
		
		/* Close Button */
		final Button closeComponentButton = new Button(shell, SWT.PUSH);
		closeComponentButton.setText("Close");
		closeComponentButton.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		closeComponentButton.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){     		
        		input = "";
        		shell.close();
        	}
        	public void widgetDefaultSelected(SelectionEvent event){
        		// Not used
        	}
        });
		
		shell.setDefaultButton(selectComponentButton);
	}
	
	private String getSelectionToString(){
		int n = treeComponentDBView.getSelectionCount();
		String out = "";
		boolean fullName = false;
		
		if(treeComponentDBView.getItemCount()>1)
			fullName = true;
		
		for(int i=0; i<n; i++){
			if(fullName)
				out+=treeComponentDBView.getSelection()[i].getParentItem().getText()+"_";
			
			if(i+1==n)
				out+=treeComponentDBView.getSelection()[i].getText();
			else
				out+=treeComponentDBView.getSelection()[i].getText()+", ";
		}
		
		return out;
	}
}

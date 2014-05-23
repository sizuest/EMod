package ch.ethz.inspire.emod.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.utils.LocalizationHandler;

public class LinkingGUI {

    private Shell shell;


    public LinkingGUI(){
	        shell = new Shell(Display.getCurrent());
	    }

	public void open(String string){
	        System.out.println("LinkingGUI opened");
	        
	        //TODO manick: shell einmitten!
	        //TODO manick: language files!!
	    	shell.setText("IO Linking " + string);

	    	shell.setLayout(new GridLayout(1, false));
	    	
	    	Text aText = new Text(shell, SWT.MULTI);
			GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
			gridData.horizontalSpan = 1;
			aText.setLayoutData(gridData);
			aText.setText("Component: " + string);
	    	
	    	//SOURCE http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/SWTTableSimpleDemo.htm Imported for function control
	    	Table aTable = new Table(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	    	gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
			gridData.horizontalSpan = 1;
	    	aTable.setLayoutData(gridData);
			aTable.setHeaderVisible(true);
	    		    //TODO manick: language file!
	    		    String[] titles = { "Input", "Link to"};

	    		    for (int i=0; i < titles.length; i++) {
	    		      TableColumn column = new TableColumn(aTable, SWT.NULL);
	    		      column.setText(titles[i]);
	    		    }

	    		    // inputs auslesen aus komponente??
	    		    for (int loopIndex = 0; loopIndex < 1; loopIndex++) {
	    		      TableItem item = new TableItem(aTable, SWT.NULL);
	    		      item.setText("Item " + loopIndex);
	    		      item.setText(0, "Item " + loopIndex);
	    		      item.setText(1, "Yes");
	    		      item.setText(2, "No");
	    		      item.setText(3, "A table item");
	    		    }

	    		    for (int loopIndex = 0; loopIndex < titles.length; loopIndex++) {
	    		      aTable.getColumn(loopIndex).pack();
	    		    }

	    		    //aTable.setBounds(25, 25, 220, 200);
	    	    	//SOURCE http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/SWTTableSimpleDemo.htm Imported for function control   	

	    	Button aButton = new Button(shell, SWT.BORDER);
	    	aButton.setText("Speichern");
			gridData = new GridData(GridData.FILL, GridData.VERTICAL_ALIGN_END, true, false);
			gridData.horizontalSpan = 1;
			aButton.setLayoutData(gridData);
			
			//TODO manick: selectionListener Button
			
			shell.pack();
			shell.open();
	    }

	    public void close(){
	    	shell.close();
	    }
}

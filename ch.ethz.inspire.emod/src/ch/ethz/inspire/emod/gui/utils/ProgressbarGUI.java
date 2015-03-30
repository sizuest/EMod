package ch.ethz.inspire.emod.gui.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.utils.LocalizationHandler;

public class ProgressbarGUI {

	private ProgressBar pb;
	private Shell shell;
	
	public ProgressbarGUI(String textString) {
		shell = new Shell(Display.getCurrent(),SWT.APPLICATION_MODAL);
		shell.setLocation(Display.getCurrent().getBounds().x/2, Display.getCurrent().getBounds().y/2);
		shell.setText(LocalizationHandler.getItem(textString));
		shell.setLayout(new GridLayout(2, false));

		Text textLoad = new Text(shell, SWT.READ_ONLY);
		textLoad.setText(LocalizationHandler.getItem(textString));
		textLoad.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, true, 1, 1));

		pb = new ProgressBar(shell, SWT.SMOOTH);
		pb.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, true, 1, 1));
		
		shell.pack();
		
		Display display = Display.getCurrent();
	    Monitor primary = display.getPrimaryMonitor();
	    Rectangle bounds = primary.getBounds();
	    Rectangle rect = shell.getBounds();
	    
	    int x = bounds.x + (bounds.width - rect.width) / 2;
	    int y = bounds.y + (bounds.height - rect.height) / 2;
	    
	    shell.setLocation(x, y);		
    	//open the new shell
		shell.open();
	}
	
 	/**
	 * update the progress bar
	 * @param progress	set from 0 to 100 as percentage of progress, when set to 100 -> progressbar is closed
	 */ 	
	public void updateProgressbar(int progress) {
		pb.setSelection(progress);
		if(progress == 100){
			shell.close();
		}
	}
	
	public void updateProgressbar(double progress){
		updateProgressbar((int)(progress));
	}
}

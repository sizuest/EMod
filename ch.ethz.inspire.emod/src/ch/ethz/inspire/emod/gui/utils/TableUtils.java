package ch.ethz.inspire.emod.gui.utils;

import java.lang.reflect.Method;
import java.text.NumberFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;

/**
 * Implements general table functions for SWT Tables
 * @author sizuest
 *
 */
public class TableUtils {
	
	/**
	 * Adds following key-event related functions: Edit cell
	 * 
	 * @param table
	 * @param fun
	 * @param funObj
	 * @return
	 */
	public static void addCellEditor(Table table, final Method fun, final Object funObj){
		//SOURCE http://www.tutorials.de/threads/in-editierbarer-swt-tabelle-ohne-eingabe-von-enter-werte-aendern.299858/
	    //create a TableCursor to navigate around the table
	    final TableCursor cursor = new TableCursor(table, SWT.NONE);
	    // create an editor to edit the cell when the user hits "ENTER"
	    // while over a cell in the table
	    final ControlEditor editor = new ControlEditor(cursor);
	    editor.grabHorizontal = true;
	    editor.grabVertical = true;
	   
	    cursor.addKeyListener(new KeyAdapter() {
	        public void keyPressed(KeyEvent e) {
	            switch(e.keyCode) {
		            case SWT.ARROW_UP:
		            case SWT.ARROW_RIGHT:
		            case SWT.ARROW_DOWN:
		            case SWT.ARROW_LEFT:
		            //an dieser stelle fehlen auch noch alle anderen tasten die
		            //ignoriert werden sollen...wie F1-12, esc,bsp,....
		                //System.out.println("Taste ignorieren...");
		                break;
		               
		            default:
		                //System.out.println("hier jetzt text editieren");
		                final Text text = new Text(cursor, SWT.NONE);
		                text.append(String.valueOf(e.character));
		                text.addKeyListener(new KeyAdapter() {
		                    public void keyPressed(KeyEvent e) {
		                        // close the text editor and copy the data over
		                        // when the user hits "ENTER"
		                        if (e.character == SWT.CR) {
		                        	try{
		                        		fun.invoke(funObj, cursor, text);
		                        	} catch(Exception e2){
		                        		System.err.print("Failed to call update function for table editor");
		                        	}
			                        text.dispose();
		                        }
		                        // close the text editor when the user hits "ESC"
		                        if (e.character == SWT.ESC) {
		                            text.dispose();
		                        }
		                    }
		                });
		                editor.setEditor(text);
		                text.setFocus();
		                break;
	            }  
	        }
	    });
	}

	public static void addCopyToClipboard(final Table table){
	    table.addKeyListener(new KeyAdapter() {
	        public void keyPressed(KeyEvent e) {
	        	if (e.stateMask == SWT.CTRL && e.keyCode == 'c') {
	                String text =TableUtils.toText(table, true);
	                Clipboard clip = new Clipboard(Display.getCurrent());
	                clip.setContents(new Object[]{text}, new TextTransfer[]{TextTransfer.getInstance()});
	                clip.dispose();
	            }
	        	
	        	if (e.stateMask == SWT.CTRL && e.keyCode == 'p') {
	                //Todo
	            }
	        }
	    });
	}
	
	public static String toText(Table table){
		return toText(table, false);
	}
	
	public static String toText(Table table, boolean selectionOnly){
		String out = "";
		String sep = ";";
		String eol = "\r\n";
		
		// Headers
		for(int i=0; i< table.getColumnCount(); i++){
			out+=table.getColumn(i).getText();
			if(table.getColumnCount()-1==i)
				out+=eol;
			else
				out+=sep;
		}
		
		// Content
		TableItem[] candidates;
		if(selectionOnly)
			candidates = table.getSelection();
		else
			candidates = table.getItems();
		
		for(TableItem item: candidates)
			for(int i=0; i< table.getColumnCount(); i++){
				out+=item.getText(i);
				if(table.getColumnCount()-1==i)
					out+=eol;
				else
					out+=sep;
			}
		
		return out;
	}
	
	public static String formatNumber(double d){
		NumberFormat numberFormatter;
		numberFormatter = NumberFormat.getNumberInstance();
		return numberFormatter.format(d);
		
	}
}

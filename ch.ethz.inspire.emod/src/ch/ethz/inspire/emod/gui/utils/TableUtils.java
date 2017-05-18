package ch.ethz.inspire.emod.gui.utils;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ch.ethz.inspire.emod.gui.AConfigGUI;

/**
 * Implements general table functions for SWT Tables
 * 
 * @author sizuest
 * 
 */
public class TableUtils {

	/**
	 * Adds following key-event related functions: Edit cell
	 * 
	 * @param table
	 * @param idx
	 * @throws Exception
	 */
	public static void addCellEditor(Table table, int[] idx) throws Exception {
		addCellEditor(table, null, null, idx);
	}


	/**
	 * Add the cell editor to a table part of a config gui
	 * This will force the listener to call {@link AConfigGUI}.wasEdited() when the table is modified
	 * @param table
	 * @param gui
	 * @param idx
	 * @throws Exception
	 */
	public static void addCellEditor(Table table, AConfigGUI gui, int[] idx)
			throws Exception {
		addCellEditor(table, AConfigGUI.class.getMethod("wasEdited"), gui, idx);
	}

	/**
	 * Adds following key-event related functions: Edit cell
	 * 
	 * @param table
	 * @param fun
	 * @param funObj
	 */
	public static void addCellEditor(Table table, final Method fun,
			final Object funObj) {
		addCellEditor(table, fun, funObj, null);
	}

	/**
	 * Adds following key-event related functions: Edit cell
	 * 
	 * @param table
	 * @param fun
	 * @param funObj
	 * @param idx
	 */
	public static void addCellEditor(final Table table, final Method fun, final Object funObj, final int[] idx) {
		// SOURCE
		// http://www.tutorials.de/threads/in-editierbarer-swt-tabelle-ohne-eingabe-von-enter-werte-aendern.299858/
		// create a TableCursor to navigate around the table
		final TableCursor cursor = new TableCursor(table, SWT.NO_FOCUS);
		// create an editor to edit the cell when the user hits "ENTER"
		// while over a cell in the table
		final ControlEditor editor = new ControlEditor(cursor);
		editor.grabHorizontal = true;
		editor.grabVertical = true;
		
		cursor.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				/* Check if the column is marked as selectable */
				if (null != idx)
					for (int i = 0; i < idx.length; i++) {
						if (cursor.getColumn() == idx[i])
							break;
						if (i == idx.length - 1)
							return;
					}
				
				/* Add input field */
				final ParameterEditor text = new ParameterEditor(SWT.NONE, cursor);
				
				// If a function is defined, call it as soon as the editor is disposed
				if(fun!=null & funObj!=null)
					text.addDisposeListener(new DisposeListener() {
						
						@Override
						public void widgetDisposed(DisposeEvent e) {
							try {
								fun.invoke(funObj);
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					});
				
				TableItem row = cursor.getRow();
				int column = cursor.getColumn();
				text.setText(row.getText(column));

				editor.setEditor(text);
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {/* Not used */}
		});

		cursor.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {

				if (null != idx)
					for (int i = 0; i < idx.length; i++) {
						if (cursor.getColumn() == idx[i])
							break;
						if (i == idx.length - 1)
							return;
					}

				switch (e.keyCode) {
				case SWT.ARROW_UP:
				case SWT.ARROW_RIGHT:
				case SWT.ARROW_DOWN:
				case SWT.ARROW_LEFT:
					// an dieser stelle fehlen auch noch alle anderen tasten die
					// ignoriert werden sollen...wie F1-12, esc,bsp,....
					// System.out.println("Taste ignorieren...");
					break;

				default:
					
				}
			}
		});
	}

	/**
	 * Copy the stated table to the clip board
	 * @param table
	 */
	public static void addCopyToClipboard(final Table table) {
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.stateMask == SWT.CTRL && e.keyCode == 'c') {
					String text = TableUtils.toText(table, false);
					Clipboard clip = new Clipboard(Display.getCurrent());
					clip.setContents(new Object[] { text }, new TextTransfer[] { TextTransfer.getInstance() });
					clip.dispose();
				}
			}
		});
	}
	
	/**
	 * Copy the clip board content to the stated table
	 * @param table
	 * @param checkForHeaders 
	 */
	public static void addPastFromClipboard(final Table table, final boolean checkForHeaders) {
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.stateMask == SWT.CTRL && e.keyCode == 'v') {
					Clipboard clip = new Clipboard(Display.getCurrent());
					try {
						String text = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
						System.out.println(text);
						
						if(checkForHeaders){
							String[] cols = text.split("\n");
							
							if(cols.length==0)
								return;
							
							if(!checkHeaders(table, cols[0].split("\t")))
								return;
							
							// Strip header
							text = text.replace(cols[0]+"\n", "");
						}
						
						fromText(table, text);
						
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} 
					clip.dispose();
				}
			}
		});
	}

	/**
	 * Convert the table to csv text
	 * @param table
	 * @return
	 */
	public static String toText(Table table) {
		return toText(table, false);
	}
	
	
	/**
	 * Tests if the tables column titles match the heads
	 * @param table
	 * @param heads
	 * @return
	 */
	public static boolean checkHeaders(Table table, String[] heads){
		TableColumn[] cols = table.getColumns();
		
		if(cols.length != heads.length)
			return false;
		
		for(int i=0; i<cols.length; i++){
			if(!(cols[i].getText().equals(heads[i])))
				return false;
		}
		
		return true;
	}
	
	/**
	 * Converts the text to table entries
	 * @param table
	 * @param text
	 * @return
	 */
	public static boolean fromText(Table table, String text){
		
		String[] cols = text.split("\n");
		
		if(cols.length==0)
			return false;
		
		String[] firstLineEntries = cols[0].split("\t");
		
		if(table.getColumnCount()!=firstLineEntries.length)
			return false;
		
		/* Clear the table */
		for(TableItem ti: table.getItems())
			ti.dispose();
		
		table.setItemCount(0);
		
		/* Write the entries */
		for(String lineText: cols){
			String[] lineEntries = lineText.split("\t");
			if(table.getColumnCount()!=lineEntries.length)
				return false;
			
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(lineEntries);
		}
		
		
		// Tabelle packen
		TableColumn[] columns = table.getColumns();
		for (int i = 0; i < columns.length; i++) {
			columns[i].pack();
		}
		
		return true;
		
		
	}

	/**
	 * Convert the selected table entries to csv text
	 * @param table
	 * @param selectionOnly
	 * @return
	 */
	public static String toText(Table table, boolean selectionOnly) {
		String out = "";
		String sep = "\t";
		String eol = "\r\n";

		// Headers
		for (int i = 0; i < table.getColumnCount(); i++) {
			out += table.getColumn(i).getText();
			if (table.getColumnCount() - 1 == i)
				out += eol;
			else
				out += sep;
		}

		// Content
		TableItem[] candidates;
		if (selectionOnly)
			candidates = table.getSelection();
		else
			candidates = table.getItems();

		for (TableItem item : candidates)
			for (int i = 0; i < table.getColumnCount(); i++) {
				out += item.getText(i);
				if (table.getColumnCount() - 1 == i)
					out += eol;
				else
					out += sep;
			}

		return out;
	}

}

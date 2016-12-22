package ch.ethz.inspire.emod.gui.utils;

import java.lang.reflect.Method;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;

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
	 * Changes the table entry given by the cursor according to the text field
	 * @param cursor
	 * @param text
	 */
	public static void changeTableEntry(TableCursor cursor, Text text) {
		TableItem row = cursor.getRow();
		int column = cursor.getColumn();
		row.setText(column, text.getText());
		text.dispose();
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
	public static void addCellEditor(Table table, final Method fun,
			final Object funObj, final int[] idx) {
		// SOURCE
		// http://www.tutorials.de/threads/in-editierbarer-swt-tabelle-ohne-eingabe-von-enter-werte-aendern.299858/
		// create a TableCursor to navigate around the table
		final TableCursor cursor = new TableCursor(table, SWT.NONE);
		// create an editor to edit the cell when the user hits "ENTER"
		// while over a cell in the table
		final ControlEditor editor = new ControlEditor(cursor);
		editor.grabHorizontal = true;
		editor.grabVertical = true;

		cursor.addKeyListener(new KeyAdapter() {
			
			private void acceptTextInput(Text text){
				changeTableEntry(cursor, text);
				if (null != fun)

					try {
						fun.invoke(funObj);
					} catch (Exception e2) {
						System.err
								.print("Failed to call update function for table editor");
					}
				text.dispose();		
			}
			
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
					final Text text = new Text(cursor, SWT.NONE);
					text.append(String.valueOf(e.character));
					text.addKeyListener(new KeyAdapter() {
						@Override
						public void keyPressed(KeyEvent e) {
							// close the text editor and copy the data over
							// when the user hits "ENTER"
							if (e.character == SWT.CR) {
								acceptTextInput(text);
							}
							// close the text editor when the user hits "ESC"
							if (e.character == SWT.ESC) {
								text.dispose();
							}
						}
					});
					text.addFocusListener(new FocusListener() {
						
						@Override
						public void focusLost(FocusEvent e) {
							acceptTextInput(text);		
						}
						
						@Override
						public void focusGained(FocusEvent e) {/* Not used */}
					});
					editor.setEditor(text);
					text.setFocus();
					break;
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
					String text = TableUtils.toText(table, true);
					Clipboard clip = new Clipboard(Display.getCurrent());
					clip.setContents(new Object[] { text },
							new TextTransfer[] { TextTransfer.getInstance() });
					clip.dispose();
				}

				if (e.stateMask == SWT.CTRL && e.keyCode == 'p') {
					// Todo
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
	 * Convert the selected table entries to csv text
	 * @param table
	 * @param selectionOnly
	 * @return
	 */
	public static String toText(Table table, boolean selectionOnly) {
		String out = "";
		String sep = ";";
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

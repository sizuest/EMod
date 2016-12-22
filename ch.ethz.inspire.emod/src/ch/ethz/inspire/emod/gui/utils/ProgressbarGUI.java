/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date: 2014-10-30 16:24:44$
 * 
 *
 * Copyright (c) 2011 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/
package ch.ethz.inspire.emod.gui.utils;

import java.text.SimpleDateFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import ch.ethz.inspire.emod.utils.LocalizationHandler;

/**
 * Implements the display of a process by its name and current status (progressbar).
 * A button can be displayed to interup the process. If the button is pressed the
 * corresponding flag will be set to true and can be accessed by the method 
 * getCancelStatus().
 * 
 * @author sizuest
 *
 */
public class ProgressbarGUI extends Composite {

	private Label textLoad;
	private String textString;
	private ProgressBar pb;
	private Button buttonCancel;

	private boolean cancelPressed = false;

	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	/**
	 * Create a new ProgressbarGUI with the status in textString
	 * This will create a new shell
	 * 
	 * @param textString
	 * @return
	 */
	public static ProgressbarGUI newProgressbarGUI(String textString) {
		final Shell shell = new Shell(Display.getCurrent(), SWT.SYSTEM_MODAL
				| SWT.CLOSE);
		shell.setLocation(Display.getCurrent().getBounds().x / 2, Display
				.getCurrent().getBounds().y / 2);
		shell.setText(LocalizationHandler.getItem(textString));

		ProgressbarGUI gui = new ProgressbarGUI(shell, textString);

		Display display = Display.getCurrent();
		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();

		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;

		shell.setLocation(x, y);
		// open the new shell
		shell.open();
		shell.pack();

		gui.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				shell.dispose();
			}
		});

		return gui;

	}
	/**
	 * Create a new ProgressbarGUI with the status in textString,
	 * placed in the existing composite parent
	 * 
	 * @param parent 
	 * @param textString
	 */
	public ProgressbarGUI(Composite parent, String textString) {
		super(parent, SWT.NONE | SWT.NO_TRIM);

		this.textString = textString;

		init();

	}

	/**
	 * Update the status text
	 * @param text
	 */
	public void setText(String text) {
		this.textString = text;
		textLoad.setText(this.textString);

		this.layout();
		this.getParent().layout();

		pb.setVisible(true);
		buttonCancel.setVisible(true);
	}

	/**
	 * Reset the GUI
	 * - no text
	 * - no progress bar
	 * - no button
	 */
	public void reset() {
		cancelPressed = false;
		this.textString = "";
		textLoad.setText(this.textString);
		pb.setSelection(0);
		pb.setVisible(false);
		buttonCancel.setVisible(false);
		pb.setEnabled(false);

		this.layout();
		this.getParent().layout();
	}

	private void init() {

		this.setLayout(new GridLayout(3, false));
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		textLoad = new Label(this, SWT.TRANSPARENT);
		textLoad.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));

		pb = new ProgressBar(this, SWT.SMOOTH);
		pb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		buttonCancel = new Button(this, SWT.PUSH);
		buttonCancel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1));
		buttonCancel.setText("X");
		buttonCancel.setBackground(new Color(getDisplay(), 255, 0, 0));
		buttonCancel.setForeground(new Color(getDisplay(), 255, 255, 255));
		FontData fontData = buttonCancel.getFont().getFontData()[0];
		Font font = new Font(
				buttonCancel.getDisplay(),
				new FontData(fontData.getName(), fontData.getHeight(), SWT.BOLD));
		buttonCancel.setFont(font);
		buttonCancel.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				cancelPressed = true;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {/* Not used */
			}
		});

		this.layout();

		reset();
	}

	/**
	 * Returns, whether the cancel button has be pressed, since it has been enabled last time
	 * @return
	 */
	public boolean getCancelStatus() {
		return cancelPressed;
	}

	/**
	 * Update the progress bar with the current value: 0<=progress<=100
	 * 
	 * @param progress
	 */
	public void updateProgressbar(int progress) {
		updateProgressbar(progress, false);
	}

	/**
	 * update the progress bar
	 * 
	 * @param progress
	 *            set from 0 to 100 as percentage of progress
	 * @param showCancelButton
	 */
	public void updateProgressbar(int progress, boolean showCancelButton) {
		pb.setVisible(true);

		try {
			pb.setSelection(progress);
			pb.setEnabled(true);

			if (!(buttonCancel.isVisible() == showCancelButton))
				buttonCancel.setVisible(showCancelButton);
			
			this.update();
			this.layout();
			this.getParent().layout();
		} catch (Exception e) {
			System.out.println("ProgressbarGUI: Update failed!");
		}
	}

	/**
	 * Update the progress bar with the current value: 0<=progress<=100
	 * 
	 * @param progress
	 */
	public void updateProgressbar(double progress) {
		updateProgressbar((int) (progress));
	}
}

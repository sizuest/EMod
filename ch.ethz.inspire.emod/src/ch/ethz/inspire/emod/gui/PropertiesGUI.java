/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
 *
 * Copyright (c) 2011 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/
package ch.ethz.inspire.emod.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * GUI to set the framework properties
 * @author sizuest
 *
 */
public class PropertiesGUI {

	private Shell shell;

	/**
	 * New properties GUI
	 */
	public PropertiesGUI() {
		shell = new Shell(Display.getCurrent());
		shell.setText(LocalizationHandler.getItem("app.gui.preferences.title"));
		shell.setLayout(new GridLayout(2, false));

		// text languages
		Text textLanguage = new Text(shell, SWT.READ_ONLY);
		textLanguage.setText(LocalizationHandler
				.getItem("app.gui.preferences.language"));
		textLanguage.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true,
				1, 1));

		// combo to let the user select the desired language
		final Combo comboLanguageValue = new Combo(shell, SWT.DROP_DOWN);
		comboLanguageValue.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
				true, 1, 1));
		String stringLanguage = PropertiesHandler.getProperty("app.language");

		// get the possible languages from the language files
		String[] comboItems = {
				LocalizationHandler.getItem("app.gui.preferences.language.de"),
				LocalizationHandler.getItem("app.gui.preferences.language.en") };
		comboLanguageValue.setItems(comboItems);

		// prefill combo according to the current settings
		if (stringLanguage.equals("de")) {
			comboLanguageValue.select(0);
		} else if (stringLanguage.equals("en")) {
			comboLanguageValue.select(1);
		}

		// text to warn the user: change of langauges requires reboot of emod
		Text textWarning = new Text(shell, SWT.READ_ONLY);
		textWarning.setText(LocalizationHandler
				.getItem("app.gui.preferences.warning"));
		textWarning.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true,
				2, 1));

		// button to save
		Button buttonSave = new Button(shell, SWT.NONE);
		buttonSave.setText(LocalizationHandler.getItem("app.gui.save"));
		// selection Listener for the button, actions when button is pressed
		buttonSave.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// according to the value of the combo, set the properties in
				// the app.config file
				String stringLanguageValue = comboLanguageValue.getText();
				if (stringLanguageValue.equals(LocalizationHandler
						.getItem("app.gui.preferences.language.de"))) {
					PropertiesHandler.setProperty("app.language", "de");
					PropertiesHandler.setProperty("app.country", "CH");
				} else if (stringLanguageValue.equals(LocalizationHandler
						.getItem("app.gui.preferences.language.en"))) {
					PropertiesHandler.setProperty("app.language", "en");
					PropertiesHandler.setProperty("app.country", "US");
				}

				shell.close();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				// Not used
			}
		});
		buttonSave.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, true,
				2, 1));
		shell.pack();

		// width and height of the shell
		Rectangle rect = shell.getBounds();
		int[] size = { 0, 0 };
		size[0] = rect.width;
		size[1] = rect.height;

		// position the shell into the middle of the last window
		int[] position;
		position = EModGUI.shellPosition();
		shell.setLocation(position[0] - size[0] / 2, position[1] - size[1] / 2);

		// open the new shell
		shell.open();
	}
}

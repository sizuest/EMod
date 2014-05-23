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
package ch.ethz.inspire.emod.gui.utils;

import java.io.File;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import ch.ethz.inspire.emod.utils.PropertiesHandler;

public class ComponentHandler {

	//Methode zur Auslesung der Maschinenkomponentenbibliothek und Aufarbeitung in einen Tree
	public static void fillTree(Tree aTree){
		
		//Maschinenkomponenten-Ordner aus Pfad auslesen
		String path = PropertiesHandler.getProperty("app.MachineComponentDBPathPrefix") + "/";
		File dir = new File(path);		
		File[] subDirs = dir.listFiles();
		Arrays.sort(subDirs);
		
		//Maschinenkomponenten-Kategorien als TreeItems schreiben
		for (int i = 0; i < subDirs.length; i++){
			TreeItem child = new TreeItem(aTree, SWT.NONE);
			child.setText(subDirs[i].getName());
						
			//Einzelne Konfigurationen der Komponenten-Kategorie aus Pfad auslesen
			String subpath = path + subDirs[i].getName() + "/";
			dir = new File(subpath);
			File[] subDirsComponents = dir.listFiles();
			Arrays.sort(subDirsComponents);
			
			//Einzelne Konfigurationen der Komponenten Kategorie unter entsprechender Kategorie ausgeben
			for(int j = 0; j < subDirsComponents.length; j++){
				TreeItem grandChild = new TreeItem(child, SWT.NONE);
				
				grandChild.setText(subDirsComponents[j].getName());
				
				//TODO manick: damit das so gelöst werden kann, muss im DnD auch das child des Trees mitkommen.
				/*
				 * 
		        String[] split = subDirsComponents[j].getName().split("_",2);
		        split[1] = split[1].replace(".xml","");
				grandChild.setText(split[1]);
				 */
			}
		} 
	}
	
	//TODO manick: implementieren
	//Methode um Komponenten aus einer Maschinenkonfiguration laden und in ModelGUI darzustellen
	public static void loadMachineConfig(){
		
	}
	
}
